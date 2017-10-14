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

#import "FBNVideoAssetView.h"

#import <AVFoundation/AVFoundation.h>

#import "FBNVideoAsset.h"

@interface FBNVideoAssetView ()

@property (nonatomic, strong, readonly) FBNVideoAsset *asset;
@property (nonatomic, strong, readonly) AVPlayer *player;
@property (nonatomic, strong, readonly) AVPlayerLayer *layer;

@end

@implementation FBNVideoAssetView

@dynamic layer;

///--------------------------------------
#pragma mark - UIView
///--------------------------------------

+ (Class)layerClass {
    return [AVPlayerLayer class];
}

///--------------------------------------
#pragma mark - Init
///--------------------------------------

- (instancetype)initWithAsset:(FBNVideoAsset *)asset {
    self = [super initWithFrame:CGRectZero];
    if (!self) return self;

    _asset = asset;

    AVPlayerItem *item = [AVPlayerItem playerItemWithAsset:asset.avAsset];
    _player = [AVPlayer playerWithPlayerItem:item];
    _player.actionAtItemEnd = AVPlayerActionAtItemEndNone;


    [[NSNotificationCenter defaultCenter] addObserverForName:AVPlayerItemDidPlayToEndTimeNotification
                                                      object:_player.currentItem
                                                       queue:[NSOperationQueue mainQueue]
                                                  usingBlock:^(NSNotification * _Nonnull note) {
                                                      [_player.currentItem seekToTime:kCMTimeZero];
                                                  }];
    [_player addObserver:self forKeyPath:@"status" options:0 context:nil];

    self.layer.videoGravity = AVLayerVideoGravityResizeAspectFill;
    self.layer.player = _player;
    [self.layer addObserver:self forKeyPath:@"readyForDisplay" options:nil context:nil];

    return self;
}

- (void)observeValueForKeyPath:(NSString *)keyPath ofObject:(id)object change:(NSDictionary<NSString *,id> *)change context:(void *)context {
    if ([keyPath isEqualToString:@"status"]) {
        switch (self.player.status) {
            case AVPlayerStatusReadyToPlay:
                if (self.window) {
                    [self.player play];
                }
                break;

            default:
                break;
        }
    }
}

- (void)dealloc {
    [self.player removeObserver:self forKeyPath:@"status"];
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

- (void)didMoveToWindow {
    [super didMoveToWindow];

    if (self.window &&
        self.player.rate == 0.0f &&
        self.player.status == AVPlayerStatusReadyToPlay) {
        [self.player play];
    } else if (!self.window && self.player.rate > 0.0f) {
        [self.player pause];
    }
}

- (CGSize)sizeThatFits:(CGSize)size {
    return self.asset.presentationSize;
}

@end
