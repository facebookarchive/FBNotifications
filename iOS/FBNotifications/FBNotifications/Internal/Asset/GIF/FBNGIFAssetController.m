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

#import "FBNGIFAssetController.h"

#import "FBNGIFAsset.h"
#import "FBNIGIFAssetViewController.h"
#import "FBNAssetContentCache.h"
#import "FBNAnimatedImage.h"

NS_ASSUME_NONNULL_BEGIN

@implementation FBNGIFAssetController

///--------------------------------------
#pragma mark - FBNAssetController
///--------------------------------------

- (void)loadAssetFromDictionary:(NSDictionary *)dictionary
                   contentCache:(nonnull FBNAssetContentCache *)cache
                     completion:(void (^)(id <FBNAsset> _Nullable asset))completion {
    if (![self isValidAssetDictionary:dictionary]) {
        completion(nil);
        return;
    }

    NSURL *url = [NSURL URLWithString:dictionary[@"url"]];
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_HIGH, 0), ^{
        NSData *data = [cache cachedDataForContentURL:url];
        UIImage *image = FBNAnimatedImageFromData(data);
        FBNGIFAsset *asset = [[FBNGIFAsset alloc] initWithImage:image];
        completion(asset);
    });
}

- (nullable NSSet<NSURL *> *)cacheURLsForAssetDictionary:(NSDictionary *)dictionary {
    if (![self isValidAssetDictionary:dictionary]) {
        return nil;
    }

    NSURL *url = [NSURL URLWithString:dictionary[@"url"]];
    return (url ? [NSSet setWithObject:url] : nil);
}

- (BOOL)isValidAssetDictionary:(NSDictionary *)dictionary {
    if (![dictionary isKindOfClass:[NSDictionary class]] ||
        ![dictionary[@"_type"] isEqualToString:FBNGIFAssetType] ||
        ![dictionary[@"url"] isKindOfClass:[NSString class]]) {
        return NO;
    }
    return YES;
}

- (nullable UIViewController<FBNContentSizeProvider> *)viewControllerForAsset:(id<FBNAsset>)asset {
    FBNGIFAsset *gifAsset = (FBNGIFAsset *)asset;
    return [[FBNGIFAssetViewController alloc] initWithAsset:gifAsset];
}

@end

NS_ASSUME_NONNULL_END
