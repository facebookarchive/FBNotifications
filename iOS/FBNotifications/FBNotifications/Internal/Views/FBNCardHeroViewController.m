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

#import "FBNCardHeroViewController.h"

#import "FBNAssetsController.h"
#import "FBNCardHeroConfiguration.h"
#import "FBNCardViewUtilities.h"
#import "FBNCardLabel.h"

NS_ASSUME_NONNULL_BEGIN

@interface FBNCardHeroViewController ()

@property (nonatomic, strong, readonly) FBNAssetsController *assetsController;
@property (nonatomic, strong, readonly) FBNCardHeroConfiguration *configuration;
@property (nonatomic, assign, readonly) CGFloat contentInset;

@property (nullable, nonatomic, strong) UIView *backgroundView;
@property (nullable, nonatomic, strong) UILabel *textLabel;

@end

@implementation FBNCardHeroViewController

///--------------------------------------
#pragma mark - Init
///--------------------------------------

- (instancetype)initWithAssetsController:(FBNAssetsController *)assetsController
                           configuration:(FBNCardHeroConfiguration *)configuration
                            contentInset:(CGFloat)contentInset {
    self = [super initWithNibName:nil bundle:nil];
    if (!self) return self;

    _assetsController = assetsController;
    _configuration = configuration;
    _contentInset = contentInset;

    return self;
}

///--------------------------------------
#pragma mark - Layout
///--------------------------------------

- (void)loadView {
    [super loadView];

    self.view.clipsToBounds = YES;
}

- (void)viewDidLoad {
    [super viewDidLoad];

    self.backgroundView = [self.assetsController viewForAsset:self.configuration.background];
    [self.view addSubview:self.backgroundView];

    if (self.configuration.content) {
        self.textLabel = [FBNCardLabel labelFromTextContent:self.configuration.content];
        [self.view addSubview:self.textLabel];
    }
}

- (void)viewWillLayoutSubviews {
    [super viewWillLayoutSubviews];

    const CGRect bounds = self.view.bounds;

    self.backgroundView.frame = bounds;

    CGRect contentBounds = CGRectInset(bounds, self.contentInset, self.contentInset);

    const CGSize textLabelSize = CGSizeMake(CGRectGetWidth(contentBounds), [self.textLabel sizeThatFits:contentBounds.size].height);
    CGRect textLabelFrame = CGRectZero;
    switch (self.configuration.contentVerticalAlignment) {
        case FBNCardContentVerticalAlignmentTop: {
            textLabelFrame = FBNRectMakeWithOriginSize(CGPointMake(self.contentInset, self.contentInset), textLabelSize);
        } break;
        case FBNCardContentVerticalAlignmentCenter: {
            textLabelFrame = FBNRectMakeWithSizeCenteredInRect(textLabelSize, contentBounds);
        } break;
        case FBNCardContentVerticalAlignmentBottom: {
            CGPoint origin = CGPointMake(self.contentInset, CGRectGetMaxY(contentBounds) - textLabelSize.height);
            textLabelFrame = FBNRectMakeWithOriginSize(origin, textLabelSize);
        } break;
        default:break;
    }

    self.textLabel.frame = textLabelFrame;
}

///--------------------------------------
#pragma mark - FBNContentSizeProvider
///--------------------------------------

- (CGSize)contentSizeThatFitsParentContainerSize:(CGSize)fitSize {
    CGSize labelFitSize = fitSize;
    labelFitSize.width -= self.contentInset * 2;
    labelFitSize.height -= self.contentInset * 2;

    CGSize textSize = [self.textLabel sizeThatFits:labelFitSize];
    if (self.textLabel.text.length > 0) {
        textSize.width += self.contentInset * 2;
        textSize.height += self.contentInset * 2;
    }

    CGSize imageSize = [self.backgroundView sizeThatFits:fitSize];
    if (imageSize.width != 0 && imageSize.height != 0) {
        CGFloat imageScale = FBNAspectFillScaleThatFits(imageSize, fitSize);
        imageSize.width *= imageScale;
        imageSize.height *= imageScale;
    }

    CGSize size = FBNSizeMax(textSize, imageSize);
    return FBNSizeAdjustToScreenScale(size, NSRoundUp);
}

@end

NS_ASSUME_NONNULL_END
