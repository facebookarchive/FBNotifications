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

#import "FBNCardActionsView.h"

#import "FBNAssetsController.h"
#import "FBNCardActionsConfiguration.h"
#import "FBNCardActionConfiguration.h"
#import "FBNCardActionButton.h"
#import "FBNCardViewUtilities.h"

@interface FBNCardActionsView ()

@property (nonatomic, strong, readonly) FBNCardActionsConfiguration *configuration;

@property (nonatomic, strong, readonly) UIView *backgroundView;
@property (nonatomic, copy, readonly) NSArray<FBNCardActionButton *> *actionButtons;

@end

@implementation FBNCardActionsView

///--------------------------------------
#pragma mark - Init
///--------------------------------------

- (instancetype)initWithConfiguration:(FBNCardActionsConfiguration *)configuration
                     assetsController:(FBNAssetsController *)assetsController
                             delegate:(id<FBNCardActionsViewDelegate>)delegate {
    self = [super init];
    if (!self) return self;

    _configuration = configuration;
    _delegate = delegate;

    id<FBNAsset> background = configuration.background;
    if (background) {
        _backgroundView = [assetsController viewForAsset:background];
        [self addSubview:_backgroundView];
    }

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
        [button addTarget:self action:@selector(_buttonAction:) forControlEvents:UIControlEventTouchUpInside];
        [actionButtons addObject:button];
        [self addSubview:button];
    }
    _actionButtons = [actionButtons copy];

    return self;
}

- (void)dealloc {
    for (FBNCardActionButton *button in _actionButtons) {
        [button removeTarget:nil action:NULL forControlEvents:UIControlEventAllEvents];
    }
}

///--------------------------------------
#pragma mark - Layout
///--------------------------------------

- (void)layoutSubviews {
    [super layoutSubviews];

    const CGRect bounds = self.bounds;

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

- (CGSize)sizeThatFits:(CGSize)fitSize {
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
#pragma mark - Button Action
///--------------------------------------

- (void)_buttonAction:(FBNCardActionButton *)button {
    [self.delegate actionsView:self didPerformButtonAction:button.action withOpenURL:button.configuration.actionURL];
}

@end
