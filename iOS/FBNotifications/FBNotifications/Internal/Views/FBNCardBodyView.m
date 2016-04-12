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

#import "FBNCardBodyView.h"

#import "FBNAssetsController.h"
#import "FBNCardBodyConfiguration.h"
#import "FBNCardTextContent.h"
#import "FBNCardLabel.h"
#import "FBNCardViewUtilities.h"

@interface FBNCardBodyView ()

@property (nonatomic, strong, readonly) FBNCardBodyConfiguration *configuration;
@property (nonatomic, assign, readonly) CGFloat contentInset;

@property (nullable, nonatomic, strong, readonly) UIView *backgroundView;
@property (nonatomic, strong, readonly) UILabel *textLabel;

@end

@implementation FBNCardBodyView

///--------------------------------------
#pragma mark - Init
///--------------------------------------

- (instancetype)initWithConfiguration:(FBNCardBodyConfiguration *)configuration
                     assetsController:(FBNAssetsController *)assetsController
                         contentInset:(CGFloat)contentInset {
    self = [super init];
    if (!self) return self;

    self.clipsToBounds = YES;

    _configuration = configuration;
    _contentInset = contentInset;

    id<FBNAsset> background = configuration.background;
    if (background) {
        _backgroundView = [assetsController viewForAsset:background];
        [self addSubview:_backgroundView];
    }

    if (configuration.content) {
        _textLabel = [FBNCardLabel labelFromTextContent:configuration.content];
        [self addSubview:_textLabel];
    }

    return self;
}

///--------------------------------------
#pragma mark - Layout
///--------------------------------------

- (void)layoutSubviews {
    [super layoutSubviews];

    _backgroundView.frame = self.bounds;

    CGRect contentBounds = CGRectInset(self.bounds, self.contentInset, self.contentInset);
    self.textLabel.frame = FBNRectAdjustToScreenScale(FBNRectMakeWithSizeCenteredInRect(contentBounds.size, self.bounds), NSRoundUp);
    self.textLabel.frame = CGRectIntegral(self.textLabel.frame);
}

- (CGSize)sizeThatFits:(CGSize)fitSize {
    fitSize.width -= self.contentInset * 2;
    fitSize.height -= self.contentInset * 2;

    CGSize size = [self.textLabel sizeThatFits:fitSize];
    size.width += self.contentInset * 2;
    size.height += self.contentInset * 2;
    return FBNSizeAdjustToScreenScale(size, NSRoundUp);
}

@end
