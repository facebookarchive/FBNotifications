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

#import "FBNAssetsController.h"

#import "FBNAsset.h"
#import "FBNAssetContentCache.h"
#import "FBNAssetController.h"

NS_ASSUME_NONNULL_BEGIN

@interface FBNAssetsController ()

@property (nonatomic, strong, readonly) NSMutableDictionary<NSString *, id<FBNAssetController>> *assetControllers;
@property (nonatomic, strong, readonly) FBNAssetContentCache *contentCache;

@end

@implementation FBNAssetsController

///--------------------------------------
#pragma mark - Init
///--------------------------------------

- (instancetype)init {
    self = [super init];
    if (!self) return self;

    _assetControllers = [NSMutableDictionary dictionary];
    _contentCache = [[FBNAssetContentCache alloc] init];

    return self;
}

///--------------------------------------
#pragma mark - Asset Controllers
///--------------------------------------

- (void)registerAssetController:(id<FBNAssetController>)controller forAssetType:(NSString *)type {
    NSAssert(!_assetControllers[type], @"Trying to register asset controller for already registered type %@", type);
    self.assetControllers[type] = controller;
}

- (nullable id<FBNAssetController>)assetControllerForAssetType:(NSString *)type {
    return self.assetControllers[type];
}

///--------------------------------------
#pragma mark - Assets
///--------------------------------------

- (nullable id<FBNAsset>)assetFromDictionary:(NSDictionary *)dictionary {
    NSString *type = [self _assetTypeFromDictionary:dictionary];
    if (!type) {
        return nil;
    }

    id<FBNAssetController> controller = [self assetControllerForAssetType:type];
    return [controller assetFromDictionary:dictionary contentCache:self.contentCache];
}

- (nullable UIView *)viewForAsset:(id<FBNAsset>)asset {
    id<FBNAssetController> controller = [self assetControllerForAssetType:asset.type];
    return [controller viewForAsset:asset];
}

- (nullable NSString *)_assetTypeFromDictionary:(NSDictionary *)dictionary {
    return dictionary[@"_type"];
}

- (nullable NSSet<NSURL *> *)_assetContentURLsFromAssetDictionary:(NSDictionary *)dictionary {
    NSString *type = [self _assetTypeFromDictionary:dictionary];
    if (!type) {
        return nil;
    }
    id<FBNAssetController> controller = [self assetControllerForAssetType:type];
    return [controller cacheURLsForAssetDictionary:dictionary];
}

///--------------------------------------
#pragma mark - Cache
///--------------------------------------

- (void)cacheAssetContentForCardPayload:(FBNCardPayload *)payload completion:(dispatch_block_t)completion {
    NSSet *urls = [self _assetContentURLsFromCardPayload:payload];
    if (!urls.count) {
        completion();
        return;
    }
    [self.contentCache cacheContentForURLs:urls completion:completion];
}

- (void)clearAssetContentCacheForCardPayload:(FBNCardPayload *)payload {
    NSSet *urls = [self _assetContentURLsFromCardPayload:payload];
    [self.contentCache clearContentForURLs:urls];
}

- (BOOL)hasCachedContentForCardPayload:(FBNCardPayload *)payload {
    NSSet<NSURL *> *urls = [self _assetContentURLsFromCardPayload:payload];
    return [self.contentCache hasCachedContentForURLs:urls];
}

- (nullable NSSet<NSURL *> *)_assetContentURLsFromCardPayload:(FBNCardPayload *)payload {
    return [self _assetContentURLsFromCollection:payload];
}

- (nullable NSSet<NSURL *> *)_assetContentURLsFromCollection:(id)collection {
    NSMutableSet<NSURL *> *contentURLs = [NSMutableSet set];
    void (^block)(NSMutableSet<NSURL *> *, id) = ^(NSMutableSet<NSURL *> *urls, id obj){
        NSSet *assetURLs = nil;
        if ([obj isKindOfClass:[NSDictionary class]]) {
            assetURLs = [self _assetContentURLsFromAssetDictionary:obj];
            if (assetURLs) {
                [urls unionSet:assetURLs];
                return;
            }
        }
        assetURLs = [self _assetContentURLsFromCollection:obj];
        if (assetURLs) {
            [urls unionSet:assetURLs];
        }
    };

    if ([collection isKindOfClass:[NSDictionary class]]) {
        [collection enumerateKeysAndObjectsUsingBlock:^(NSString *_, id obj, BOOL *__) {
            block(contentURLs, obj);
        }];
    } else if ([collection isKindOfClass:[NSArray class]]) {
        for (id obj in collection) {
            block(contentURLs, obj);
        }
    }
    return [contentURLs copy];
}

@end

NS_ASSUME_NONNULL_END
