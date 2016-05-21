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

#import "FBNCardActionsViewController.h"

#import "FBNAssetsController.h"
#import "FBNCardActionsConfiguration.h"
#import "FBNCardActionConfiguration.h"
#import "FBNCardActionButton.h"
#import "FBNCardViewUtilities.h"

NS_ASSUME_NONNULL_BEGIN

@interface FBNCardActionsViewController ()

@property (nonatomic, strong, readonly) FBNAssetsController *assetsController;
@property (nonatomic, strong, readonly) FBNCardActionsConfiguration *configuration;

@property (nullable, nonatomic, strong) UIView *backgroundView;
@property (nullable, nonatomic, copy) NSArray<FBNCardActionButton *> *actionButtons;

@end

@implementation FBNCardActionsViewController

///--------------------------------------
#pragma mark - Init/Dealloc
///--------------------------------------

- (instancetype)initWithAssetsController:(FBNAssetsController *)assetsController
                           configuration:(FBNCardActionsConfiguration *)configuration {
    self = [super init];
    if (!self) return self;

    _assetsController = assetsController;
    _configuration = configuration;

    return self;
}

- (void)dealloc {
    for (FBNCardActionButton *button in self.actionButtons) {
        [button removeTarget:nil action:NULL forControlEvents:UIControlEventAllEvents];
    }
}

///--------------------------------------
#pragma mark - View
///--------------------------------------

- (void)viewDidLoad {
    [super viewDidLoad];

    id<FBNAsset> background = self.configuration.background;
    if (background) {
        self.backgroundView = [self.assetsController viewForAsset:background];
        [self.view addSubview:self.backgroundView];
    }

    self.actionButtons = [[self class] _actionButtonsFromConfiguration:self.configuration];
    for (FBNCardActionButton *button in self.actionButtons) {
        [button addTarget:self action:@selector(_buttonAction:) forControlEvents:UIControlEventTouchUpInside];
        [self.view addSubview:button];
    }
}

///--------------------------------------
#pragma mark - Layout
///--------------------------------------

- (void)viewDidLayoutSubviews {
    [super viewDidLayoutSubviews];

    const CGRect bounds = self.view.bounds;

    self.backgroundView.frame = bounds;

    CGSize buttonSize = { .width = 0.0f, .height = self.configuration.height };
    switch (self.configuration.layoutStyle) {
        case FBNCardActionsLayoutStyleHorizontal: {
            if (self.configuration.style == FBNCardActionsStyleAttached) {
                buttonSize.width = CGRectGetWidth(bounds) - self.configuration.contentInset * (self.actionButtons.count + 1); // Left + Right + Inter-button
            } else {
                buttonSize.width = CGRectGetWidth(bounds) - self.configuration.contentInset * (self.actionButtons.count - 1); // Inter-button only
            }
            buttonSize.width /= self.actionButtons.count;
        } break;
        case FBNCardActionsLayoutStyleVertical: {
            if (self.configuration.style == FBNCardActionsStyleAttached) {
                buttonSize.width = CGRectGetWidth(bounds) - (self.configuration.contentInset * 2); // Left + Right
            } else {
                buttonSize.width = CGRectGetWidth(bounds);
            }
        } break;
        default:break;
    }

    CGPoint buttonOrigin = { .x = 0.0f, .y = self.configuration.topInset };
    CGRect buttonFrame = FBNRectAdjustToScreenScale(FBNRectMakeWithOriginSize(buttonOrigin, buttonSize), NSRoundUp);

    if (self.configuration.style == FBNCardActionsStyleAttached) {
        buttonFrame.origin.x = self.configuration.contentInset;
    }

    for (FBNCardActionButton *button in self.actionButtons) {
        button.frame = FBNRectAdjustToScreenScale(buttonFrame, NSRoundUp);

        switch (self.configuration.layoutStyle) {
            case FBNCardActionsLayoutStyleHorizontal: {
                buttonFrame.origin.x = CGRectGetMaxX(buttonFrame) + self.configuration.contentInset; // Previous button + Inset
            } break;
            case FBNCardActionsLayoutStyleVertical: {
                buttonFrame.origin.y = CGRectGetMaxY(buttonFrame) + self.configuration.contentInset; // Previous button + Inset
            } break;
            default:break;
        }
    }
}

///--------------------------------------
#pragma mark - FBNContentSizeProvider
///--------------------------------------

- (CGSize)contentSizeThatFitsParentContainerSize:(CGSize)fitSize {
    CGSize size = CGSizeMake(fitSize.width, self.configuration.topInset);
    switch (self.configuration.layoutStyle) {
        case FBNCardActionsLayoutStyleVertical: {
            size.height += self.configuration.height * self.actionButtons.count; // All the buttons
            size.height += self.configuration.contentInset * self.actionButtons.count; // Inter-button + Bottom
        } break;
        case FBNCardActionsLayoutStyleHorizontal: {
            size.height += self.configuration.height; // Single button
            size.height += self.configuration.contentInset; // Bottom inset
        } break;
        default: break;
    }
    return FBNSizeAdjustToScreenScale(size, NSRoundUp);
}

///--------------------------------------
#pragma mark - Buttons
///--------------------------------------

+ (NSArray<FBNCardActionButton *> *)_actionButtonsFromConfiguration:(FBNCardActionsConfiguration *)configuration {
    NSMutableArray<FBNCardActionButton *> *actionButtons = [NSMutableArray arrayWithCapacity:configuration.actions.count];
    FBNCardButtonAction buttonAction = FBNCardButtonActionPrimary;
    for (FBNCardActionConfiguration *actionConfiguration in configuration.actions) {
        FBNCardActionButton *button = nil;
        if (actionConfiguration.actionURL != nil) {
            button = [FBNCardActionButton buttonFromConfiguration:actionConfiguration
                                                 withCornerRadius:configuration.cornerRadius
                                                           action:buttonAction];
            if (buttonAction == FBNCardButtonActionPrimary) {
                buttonAction = FBNCardButtonActionSecondary;
            }
        } else {
            button = [FBNCardActionButton buttonFromConfiguration:actionConfiguration
                                                 withCornerRadius:configuration.cornerRadius
                                                           action:FBNCardButtonActionDismiss];
        }
        [actionButtons addObject:button];
    }
    return actionButtons;
}

- (void)_buttonAction:(FBNCardActionButton *)button {
    [self.delegate actionsViewController:self didPerformButtonAction:button.action withOpenURL:button.configuration.actionURL];
}

@end

NS_ASSUME_NONNULL_END
