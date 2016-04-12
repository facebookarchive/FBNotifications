// Copyright (c) 2016-present, Facebook, Inc. All rights reserved.
//
// You are hereby granted a non-exclusive, worldwide, royalty-free license to use,
// copy, modify, and distribute this software in source code or binary form for use
// in connection with the web services and APIs provided by Facebook.
//
// As with any software that integrates with the Facebook platform, your use of
// this software is subject to the Facebook Developer Principles and Policies
// [http://developers.facebook.com/policy/]. This copyright notice shall be
// included in all copies or substantial portions of the software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
// FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
// COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
// IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
// CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

#import "FBNAssetContentCache.h"

#import <UIKit/UIKit.h>

#import "FBNAssetsController.h"
#import "FBNCardHash.h"
#import "FBNAssetContentCacheOperation.h"

@interface FBNAssetContentCache () <NSURLSessionDownloadDelegate>

@property (nonatomic, strong, readonly) NSURLSession *session;

@property (nonatomic, copy, readonly) NSMutableDictionary<NSString *, NSMutableSet<FBNAssetContentCacheOperation *> *> *cacheOperations;
@property (nonatomic, copy) NSMutableSet<NSString *> *cachedKeys;
@property (nonatomic, strong, readonly) dispatch_queue_t synchronizationQueue;

@property (nonatomic, assign) UIBackgroundTaskIdentifier currentBackgroundTask;

@end

@implementation FBNAssetContentCache

///--------------------------------------
#pragma mark - Init
///--------------------------------------

- (instancetype)init {
    self = [super init];
    if (!self) return self;

    NSURLSessionConfiguration *configuration = [NSURLSessionConfiguration defaultSessionConfiguration];
    _session = [NSURLSession sessionWithConfiguration:configuration delegate:self delegateQueue:nil];

    _cacheOperations = [NSMutableDictionary dictionary];
    _synchronizationQueue = dispatch_queue_create("com.facebook.cards.cache.sync", DISPATCH_QUEUE_SERIAL);

    _currentBackgroundTask = UIBackgroundTaskInvalid;

    return self;
}

///--------------------------------------
#pragma mark - Cache
///--------------------------------------

- (void)cacheContentForURLs:(NSSet<NSURL *> *)urls completion:(dispatch_block_t)completion {
    [self _beginBackgroundTask];

    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        FBNAssetContentCacheOperation *operation = [[FBNAssetContentCacheOperation alloc] initWithContentURLs:urls completion:completion];
        [self _startDownloadTasksForCacheOperation:operation];
    });
}

- (void)clearContentForURLs:(nullable NSSet<NSURL *> *)urls {
    if (!urls.count) {
        return;
    }

    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        for (NSURL *url in urls) {
            NSString *cacheKey = [self _cacheKeyForContentURL:url];
            [[NSFileManager defaultManager] removeItemAtPath:[self _cacheFilePathForContentWithCacheKey:cacheKey] error:nil];
            dispatch_async(_synchronizationQueue, ^{
                [self.cachedKeys removeObject:cacheKey];
            });
        }
    });
}

///--------------------------------------
#pragma mark - Getters
///--------------------------------------

- (NSData *)cachedDataForContentURL:(NSURL *)url {
    NSString *cacheKey = [self _cacheKeyForContentURL:url];
    return [NSData dataWithContentsOfFile:[self _cacheFilePathForContentWithCacheKey:cacheKey]
                                  options:NSDataReadingMappedIfSafe
                                    error:nil];
}

- (BOOL)hasCachedContentForURLs:(nullable NSSet<NSURL *> *)urls {
    for (NSURL *url in urls) {
        NSString *cacheKey = [self _cacheKeyForContentURL:url];
        if (![self _hasCachedDataForContentWithCacheKey:cacheKey]) {
            return NO;
        }
    }
    return YES;
}

- (BOOL)_hasCachedDataForContentWithCacheKey:(NSString *)key {
    return [self.cachedKeys containsObject:key];
}

- (NSString *)_cacheFilesFolderPath {
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES);
    return [paths.firstObject stringByAppendingPathComponent:@"FBNotifications"];
}

- (NSString *)_cacheFilePathForContentWithCacheKey:(NSString *)key {
    return [[self _cacheFilesFolderPath] stringByAppendingPathComponent:key];
}

///--------------------------------------
#pragma mark - Private
///--------------------------------------

///--------------------------------------
#pragma mark - Accessors
///--------------------------------------

- (NSMutableSet *)cachedKeys {
    if (!_cachedKeys) {
        NSArray *existingFiles = [[NSFileManager defaultManager] contentsOfDirectoryAtPath:[self _cacheFilesFolderPath] error:nil];
        _cachedKeys = [NSMutableSet setWithArray:existingFiles];
    }
    return _cachedKeys;
}

///--------------------------------------
#pragma mark - Hash
///--------------------------------------

- (NSString *)_cacheKeyForContentURL:(NSURL *)url {
    return FBMD5HashFromString(url.absoluteString);
}

///--------------------------------------
#pragma mark - Content
///--------------------------------------

- (void)_startDownloadTasksForCacheOperation:(FBNAssetContentCacheOperation *)operation {
    dispatch_async(_synchronizationQueue, ^{
        NSUInteger scheduledCount = 0;
        NSSet *cacheURLs = [operation.pendingCacheURLs copy];
        for (NSURL *url in cacheURLs) {
            NSString *cacheKey = [self _cacheKeyForContentURL:url];
            if ([self _hasCachedDataForContentWithCacheKey:cacheKey]) {
                [operation.pendingCacheURLs removeObject:url];
                continue;
            }

            NSMutableSet<FBNAssetContentCacheOperation *> *cacheOperations = self.cacheOperations[cacheKey];
            if (cacheOperations) {
                [cacheOperations addObject:operation];
            } else {
                cacheOperations = [NSMutableSet setWithObject:operation];
                self.cacheOperations[cacheKey] = cacheOperations;

                [[self.session downloadTaskWithURL:url] resume];
            }

            scheduledCount++;
        }

        BOOL endBackgroundTask = (self.cacheOperations.count == 0);
        dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
            if (scheduledCount == 0 && operation.completion) {
                dispatch_async(dispatch_get_main_queue(), operation.completion);
            }

            if (endBackgroundTask) {
                [self _endBackgroundTask];
            }
        });
    });
}

- (void)_didDownloadContentWithCacheKey:(NSString *)cacheKey toURL:(NSURL *)url {
    NSString *cacheFolderPath = [self _cacheFilesFolderPath];
    NSFileManager *fileManager = [NSFileManager defaultManager];
    if (![fileManager fileExistsAtPath:cacheFolderPath]) {
        [fileManager createDirectoryAtPath:cacheFolderPath withIntermediateDirectories:YES attributes:nil error:nil];
    }

    NSString *targetPath = [self _cacheFilePathForContentWithCacheKey:cacheKey];
    [fileManager moveItemAtPath:url.path toPath:targetPath error:nil]; // TODO: Track Error
}

- (void)_didFinishDownloadTaskWithCacheKey:(NSString *)cacheKey fromURL:(NSURL *)url {
    dispatch_async(_synchronizationQueue, ^{
        [self.cachedKeys addObject:cacheKey];

        NSSet<FBNAssetContentCacheOperation *> *operations = self.cacheOperations[cacheKey];
        NSMutableSet<FBNAssetContentCacheOperation *> *finishedOperations = [NSMutableSet set];
        for (FBNAssetContentCacheOperation *operation in operations) {
            [operation.pendingCacheURLs removeObject:url];
            if (operation.pendingCacheURLs.count == 0) {
                [finishedOperations addObject:operation];
            }
        }
        [self.cacheOperations removeObjectForKey:cacheKey];

        BOOL endBackgroundTask = (self.cacheOperations.count == 0);
        dispatch_async(dispatch_get_main_queue(), ^{
            for (FBNAssetContentCacheOperation *operation in finishedOperations) {
                if (operation.completion) {
                    operation.completion();
                }
            }
            if (endBackgroundTask) {
                [self _endBackgroundTask];
            }
        });
    });
}

///--------------------------------------
#pragma mark - NSURLSessionDownloadDelegate
///--------------------------------------

- (void)URLSession:(NSURLSession *)session downloadTask:(NSURLSessionDownloadTask *)downloadTask didFinishDownloadingToURL:(NSURL *)location {
    NSString *cacheKey = [self _cacheKeyForContentURL:downloadTask.originalRequest.URL];
    [self _didDownloadContentWithCacheKey:cacheKey toURL:location];
}

- (void)URLSession:(NSURLSession *)session task:(NSURLSessionTask *)task didCompleteWithError:(NSError *)error {
    NSURL *url = task.originalRequest.URL;
    NSString *cacheKey = [self _cacheKeyForContentURL:url];
    [self _didFinishDownloadTaskWithCacheKey:cacheKey fromURL:url];
}

///--------------------------------------
#pragma mark - Background Task
///--------------------------------------

- (void)_beginBackgroundTask {
    if (self.currentBackgroundTask == UIBackgroundTaskInvalid) {
        self.currentBackgroundTask = [[UIApplication sharedApplication] beginBackgroundTaskWithExpirationHandler:^{
            [self _endBackgroundTask];
        }];
    }
}

- (void)_endBackgroundTask {
    if (self.currentBackgroundTask != UIBackgroundTaskInvalid) {
        UIBackgroundTaskIdentifier task = self.currentBackgroundTask;
        self.currentBackgroundTask = UIBackgroundTaskInvalid;
        [[UIApplication sharedApplication] endBackgroundTask:task];
    }
}

@end
