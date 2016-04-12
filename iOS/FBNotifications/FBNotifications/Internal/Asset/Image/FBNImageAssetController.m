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
#import "FBNAssetContentCache.h"

NS_ASSUME_NONNULL_BEGIN

@implementation FBNImageAssetController

///--------------------------------------
#pragma mark - FBNAssetController
///--------------------------------------

- (nullable id<FBNAsset>)assetFromDictionary:(NSDictionary *)dictionary contentCache:(FBNAssetContentCache *)cache {
    if (![self isValidAssetDictionary:dictionary]) {
        return nil;
    }

    NSURL *url = [NSURL URLWithString:dictionary[@"url"]];
    UIImage *image = [[UIImage alloc] initWithData:[cache cachedDataForContentURL:url]];
    return [[FBNImageAsset alloc] initWithImage:image];
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

- (nullable UIView *)viewForAsset:(id<FBNAsset>)asset {
    FBNImageAsset *imageAsset = (FBNImageAsset *)asset;
    UIImageView *view = [[UIImageView alloc] initWithImage:imageAsset.image];
    view.contentMode = UIViewContentModeScaleAspectFill;
    return view;
}

@end

NS_ASSUME_NONNULL_END
