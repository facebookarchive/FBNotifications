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

#import "FBNImageAssetController.h"

#import "FBNImageAsset.h"
#import "FBNImageAssetViewController.h"
#import "FBNAssetContentCache.h"

NS_ASSUME_NONNULL_BEGIN

@implementation FBNImageAssetController

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
#if __has_include(<SDWebImage/UIImageView+WebCache.h>)
    //We let SDWebImage handle the caching
    FBNImageAsset *asset = [[FBNImageAsset alloc] initWithURL:url];
    completion(asset);

#else
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_HIGH, 0), ^{
        NSData *data = [cache cachedDataForContentURL:url];
        UIImage *image = [[UIImage alloc] initWithData:data];
        FBNImageAsset *asset = [[FBNImageAsset alloc] initWithImage:image];
        completion(asset);
    });
#endif
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
        ![dictionary[@"_type"] isEqualToString:FBNImageAssetType] ||
        ![dictionary[@"url"] isKindOfClass:[NSString class]]) {
        return NO;
    }
    return YES;
}

- (nullable UIViewController<FBNContentSizeProvider> *)viewControllerForAsset:(id<FBNAsset>)asset {
    FBNImageAsset *imageAsset = (FBNImageAsset *)asset;
    return [[FBNImageAssetViewController alloc] initWithAsset:imageAsset];
}

@end

NS_ASSUME_NONNULL_END
