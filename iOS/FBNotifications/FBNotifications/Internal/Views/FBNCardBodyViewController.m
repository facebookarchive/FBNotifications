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

#import "FBNCardBodyViewController.h"

#import "FBNAssetsController.h"
#import "FBNCardBodyConfiguration.h"
#import "FBNCardTextContent.h"
#import "FBNCardLabel.h"
#import "FBNCardViewUtilities.h"

NS_ASSUME_NONNULL_BEGIN

@interface FBNCardBodyViewController ()

@property (nonatomic, strong, readonly) FBNAssetsController *assetsController;
@property (nonatomic, strong, readonly) FBNCardBodyConfiguration *configuration;
@property (nonatomic, assign, readonly) CGFloat contentInset;

@property (nullable, nonatomic, strong) UIView *backgroundView;
@property (nullable, nonatomic, strong) UILabel *textLabel;

@end

@implementation FBNCardBodyViewController

///--------------------------------------
#pragma mark - Init
///--------------------------------------

- (instancetype)initWithAssetsController:(FBNAssetsController *)controller
                           configuration:(FBNCardBodyConfiguration *)configuration
                            contentInset:(CGFloat)contentInset {
    self = [super init];
    if (!self) return self;

    _assetsController = controller;
    _configuration = configuration;
    _contentInset = contentInset;

    return self;
}

///--------------------------------------
#pragma mark - View
///--------------------------------------

- (void)loadView {
    [super loadView];

    self.view.clipsToBounds = YES;
}

- (void)viewDidLoad {
    [super viewDidLoad];


    id<FBNAsset> background = self.configuration.background;
    if (background) {
        self.backgroundView = [self.assetsController viewForAsset:background];
        [self.view addSubview:self.backgroundView];
    }

    if (self.configuration.content) {
        self.textLabel = [FBNCardLabel labelFromTextContent:self.configuration.content];
        [self.view addSubview:self.textLabel];
    }
}

///--------------------------------------
#pragma mark - Layout
///--------------------------------------

- (void)viewDidLayoutSubviews {
    [super viewDidLayoutSubviews];

    const CGRect bounds = self.view.bounds;

    self.backgroundView.frame = bounds;

    CGRect contentBounds = CGRectInset(bounds, self.contentInset, self.contentInset);
    self.textLabel.frame = FBNRectAdjustToScreenScale(FBNRectMakeWithSizeCenteredInRect(contentBounds.size, bounds), NSRoundUp);
    self.textLabel.frame = CGRectIntegral(self.textLabel.frame);
}

///--------------------------------------
#pragma mark - FBNContentSizeProvider
///--------------------------------------

- (CGSize)contentSizeThatFitsParentContainerSize:(CGSize)fitSize {
    fitSize.width -= self.contentInset * 2;
    fitSize.height -= self.contentInset * 2;

    CGSize size = [self.textLabel sizeThatFits:fitSize];
    size.width += self.contentInset * 2;
    size.height += self.contentInset * 2;
    return FBNSizeAdjustToScreenScale(size, NSRoundUp);
}

@end

NS_ASSUME_NONNULL_END
