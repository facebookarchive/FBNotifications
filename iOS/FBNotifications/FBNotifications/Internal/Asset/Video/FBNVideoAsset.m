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

#import "FBNVideoAsset.h"

#import <AVFoundation/AVFoundation.h>
#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

NSString *const FBNVideoAssetType = @"Video";

@interface FBNVideoAsset ()

@property (nonatomic, assign, readwrite) CGSize presentationSize;

@end

@implementation FBNVideoAsset

///--------------------------------------
#pragma mark - Init
///--------------------------------------

+ (void)loadFromURL:(NSURL *)url completion:(void(^)(FBNVideoAsset *_Nullable asset))completion {
    AVAsset *asset = [AVURLAsset assetWithURL:url];
    if (!asset) {
        completion(nil);
        return;
    }
    [asset loadValuesAsynchronouslyForKeys:@[ @"tracks" ] completionHandler:^{
        NSArray<AVAssetTrack *> *tracks = [asset tracksWithMediaType:AVMediaTypeVideo];
        AVAssetTrack *videoTrack = tracks.firstObject;
        if (!videoTrack) {
            completion(nil);
            return;
        }

        [videoTrack loadValuesAsynchronouslyForKeys:@[ @"naturalSize", @"preferredTransform" ] completionHandler:^{
            CGSize size = CGSizeApplyAffineTransform(videoTrack.naturalSize, videoTrack.preferredTransform);
            FBNVideoAsset *videoAsset = [[self alloc] initWithAsset:asset presentationSize:size];
            completion(videoAsset);
        }];
    }];
}

- (instancetype)initWithAsset:(AVAsset *)asset presentationSize:(CGSize)size {
    self = [super init];
    if (!self) return self;

    _avAsset = asset;
    _presentationSize = size;

    return self;
}

///--------------------------------------
#pragma mark - FBNAsset
///--------------------------------------

- (NSString *)type {
    return FBNVideoAssetType;
}

@end

NS_ASSUME_NONNULL_END
