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

#import "FBNCardViewController.h"
#import "FBNCardViewController_Internal.h"

#import "FBNAssetsController.h"
#import "FBNAssetContentCache.h"
#import "FBNCardHeroView.h"
#import "FBNCardBodyView.h"
#import "FBNCardActionsView.h"
#import "FBNCardDismissButton.h"
#import "FBNCardViewUtilities.h"
#import "FBNCardConfiguration.h"
#import "FBNCardHeroConfiguration.h"
#import "FBNCardBodyConfiguration.h"
#import "FBNCardActionsConfiguration.h"
#import "FBNCardActionConfiguration.h"
#import "FBNCardAppEventsLogger.h"
#import "FBNCardColor.h"

@interface FBNCardViewController () <FBNCardActionsViewDelegate>

@property (nullable, nonatomic, copy, readonly) NSString *campaignIdentifier;
@property (nonatomic, strong, readonly) FBNAssetsController *assetsController;

@property (nonatomic, strong) FBNCardConfiguration *configuration;

@property (nonatomic, strong) UIView *contentView;
@property (nullable, nonatomic, strong) UIActivityIndicatorView *loadingIndicatorView;
@property (nullable, nonatomic, strong) FBNCardHeroView *heroView;
@property (nullable, nonatomic, strong) FBNCardBodyView *bodyView;
@property (nullable, nonatomic, strong) FBNCardActionsView *actionsView;
@property (nullable, nonatomic, strong) FBNCardDismissButton *dismissButton;

@end

@implementation FBNCardViewController

///--------------------------------------
#pragma mark - Init
///--------------------------------------

- (instancetype)initWithPushCardPayload:(FBNCardPayload *)payload
                     campaignIdentifier:(nullable NSString *)campaignIdentifier
                       assetsController:(FBNAssetsController *)assetsController {
    self = [super initWithNibName:nil bundle:nil];
    if (!self) return self;

    _payload = [payload copy];
    _campaignIdentifier = [campaignIdentifier copy];
    _assetsController = assetsController;

    [self _reloadConfiguration];

    self.modalPresentationStyle = UIModalPresentationOverFullScreen;
    self.modalTransitionStyle = UIModalTransitionStyleCrossDissolve;

    return self;
}

- (void)dealloc {
    [self.dismissButton removeTarget:nil action:nil forControlEvents:UIControlEventAllEvents];
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

///--------------------------------------
#pragma mark - View
///--------------------------------------

- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];

    if ([UIApplication sharedApplication].applicationState != UIApplicationStateBackground) {
        [self _logPushOpen];
    } else {
        [[NSNotificationCenter defaultCenter] addObserver:self
                                                 selector:@selector(_applicationWillEnterForeground)
                                                     name:UIApplicationWillEnterForegroundNotification
                                                   object:nil];
    }
}

///--------------------------------------
#pragma mark - Configuration
///--------------------------------------

- (void)_reloadConfiguration {
    _configuration = [FBNCardConfiguration configurationFromDictionary:self.payload assetsController:self.assetsController];
    if ([self.assetsController hasCachedContentForCardPayload:self.payload]) {
        if ([self isViewLoaded]) {
            [self _reloadSubviews];
        }
    } else {
        __weak typeof(self) wself = self;
        [self.assetsController cacheAssetContentForCardPayload:self.payload completion:^{
            [wself _reloadConfiguration];
        }];
    }
}

///--------------------------------------
#pragma mark - Subviews
///--------------------------------------

- (void)_reloadSubviews {
    self.view.backgroundColor = self.configuration.backdropColor;

    self.contentView = [[UIView alloc] initWithFrame:CGRectZero];
    self.contentView.layer.cornerRadius = self.configuration.cornerRadius;
    self.contentView.clipsToBounds = YES;
    [self.view addSubview:self.contentView];

    if ([self.assetsController hasCachedContentForCardPayload:self.payload]) {
        [self.loadingIndicatorView stopAnimating];
        [self.loadingIndicatorView removeFromSuperview];
        self.loadingIndicatorView = nil;

        [self _reloadContentViews];
    } else {
        FBNCardContrastColor contrastColor = FBNCardContrastColorForColor(self.view.backgroundColor);
        UIActivityIndicatorViewStyle indicatorViewStyle = (contrastColor == FBNCardContrastColorBlack ?
                                                           UIActivityIndicatorViewStyleGray :
                                                           UIActivityIndicatorViewStyleWhite);
        self.loadingIndicatorView = [[UIActivityIndicatorView alloc] initWithActivityIndicatorStyle:indicatorViewStyle];
        [self.view addSubview:self.loadingIndicatorView];
        [self.loadingIndicatorView startAnimating];
    }
}

- (void)_reloadContentViews {
    if (self.configuration.heroConfiguration) {
        self.heroView = [[FBNCardHeroView alloc] initWithConfiguration:self.configuration.heroConfiguration
                                                      assetsController:self.assetsController
                                                          contentInset:self.configuration.contentInset];
        [self.contentView addSubview:self.heroView];
    }
    if (self.configuration.bodyConfiguration) {
        self.bodyView = [[FBNCardBodyView alloc] initWithConfiguration:self.configuration.bodyConfiguration
                                                      assetsController:self.assetsController
                                                          contentInset:self.configuration.contentInset];
        [self.contentView addSubview:self.bodyView];
    }
    if (self.configuration.actionsConfiguration) {
        self.actionsView = [[FBNCardActionsView alloc] initWithConfiguration:self.configuration.actionsConfiguration
                                                            assetsController:self.assetsController
                                                                    delegate:self];
        switch (self.configuration.actionsConfiguration.style) {
            case FBNCardActionsStyleAttached:
                [self.contentView addSubview:self.actionsView];
                break;
            case FBNCardActionsStyleDetached:
                [self.view addSubview:self.actionsView];
                break;
            default:break;
        }
    }
    [self _reloadDismissButton];
}

- (void)_reloadDismissButton {
    BOOL showsDismissButton = YES;
    for (FBNCardActionConfiguration *action in self.configuration.actionsConfiguration.actions) {
        if (!action.actionURL) {
            showsDismissButton = NO;
            break;
        }
    }
    if (showsDismissButton) {
        self.dismissButton = [[FBNCardDismissButton alloc] initWithFrame:CGRectZero];
        self.dismissButton.imageColor = self.configuration.dismissButtonColor;
        [self.dismissButton addTarget:self action:@selector(_dismissButtonAction) forControlEvents:UIControlEventTouchUpInside];
        [self.view addSubview:self.dismissButton];
    }
}

///--------------------------------------
#pragma mark - UIViewController
///--------------------------------------

- (void)loadView {
    [super loadView];

    self.view.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
}

- (void)viewDidLoad {
    [super viewDidLoad];

    [self _reloadSubviews];
}

- (void)viewWillLayoutSubviews {
    [super viewWillLayoutSubviews];

    CGRect bounds = self.view.bounds;
    if (self.configuration.size == FBNCardSizeLarge) {
        bounds.size.height -= self.topLayoutGuide.length;
        bounds.origin.y += self.topLayoutGuide.length;
    }

    CGSize availableSize = FBNCardLayoutSizeThatFits(self.configuration.size, bounds.size);

    const CGSize bodySize = CGSizeMake(availableSize.width, [self.bodyView sizeThatFits:availableSize].height);
    availableSize.height -= bodySize.height;

    const CGSize actionsSize = CGSizeMake(availableSize.width, [self.actionsView sizeThatFits:availableSize].height);
    availableSize.height -= actionsSize.height;

    CGSize heroSize = availableSize;
    if (self.configuration.heroConfiguration.height == FBNCardHeroHeightUnspecified) {
        heroSize.height = [self.heroView sizeThatFits:availableSize].height;
    } else {
        heroSize.height *= self.configuration.heroConfiguration.height;
    }
    heroSize = FBNSizeAdjustToScreenScale(heroSize, NSRoundUp);

    CGSize contentSize = CGSizeMake(availableSize.width, bodySize.height + heroSize.height);

    const CGRect heroFrame = FBNRectMakeWithOriginSize(CGPointZero, heroSize);
    const CGRect bodyFrame = FBNRectMakeWithOriginSize(CGPointMake(CGRectGetMinX(heroFrame), CGRectGetMaxY(heroFrame)), bodySize);
    CGRect actionsFrame = FBNRectMakeWithOriginSize(CGPointZero, actionsSize);
    CGRect contentFrame = CGRectZero;
    switch (self.configuration.actionsConfiguration.style) {
        case FBNCardActionsStyleAttached: {
            actionsFrame.origin = CGPointMake(CGRectGetMinX(heroFrame), CGRectGetMaxY(bodyFrame));
            contentSize.height += actionsSize.height;
            contentFrame = FBNRectMakeWithSizeCenteredInRect(contentSize, bounds);
        }
            break;
        case FBNCardActionsStyleDetached: {
            CGPoint contentOrigin = FBNRectMakeWithSizeCenteredInRect(CGSizeMake(contentSize.width, contentSize.height + actionsSize.height),
                                                                     bounds).origin;
            contentFrame = FBNRectMakeWithOriginSize(contentOrigin, contentSize);
            actionsFrame.origin = CGPointMake(CGRectGetMinX(contentFrame), CGRectGetMaxY(contentFrame));
        }
            break;
        default:break;
    }
    CGRect dismissButtonFrame = FBNRectMakeWithOriginSize(CGPointZero, [self.dismissButton sizeThatFits:bounds.size]);
    dismissButtonFrame.origin.x = CGRectGetMaxX(contentFrame) - CGRectGetWidth(dismissButtonFrame) - self.configuration.contentInset;
    dismissButtonFrame.origin.y = CGRectGetMinY(contentFrame) + self.configuration.contentInset;

    self.contentView.frame = contentFrame;
    self.heroView.frame = heroFrame;
    self.bodyView.frame = bodyFrame;
    self.actionsView.frame = actionsFrame;
    self.dismissButton.frame = dismissButtonFrame;
    self.loadingIndicatorView.center = CGPointMake(CGRectGetMidX(bounds), CGRectGetMidY(bounds));
}

///--------------------------------------
#pragma mark - Application State Changes
///--------------------------------------

- (void)_applicationWillEnterForeground {
    [self _logPushOpen];
    [[NSNotificationCenter defaultCenter] removeObserver:self name:UIApplicationWillEnterForegroundNotification object:nil];
}

///--------------------------------------
#pragma mark - Logging Events
///--------------------------------------

- (void)_logPushOpen {
    [FBNCardAppEventsLogger logCardOpenWithCampaignIdentifier:self.campaignIdentifier];
}

///--------------------------------------
#pragma mark - Dismiss
///--------------------------------------

- (void)_dismissFromButtonAction:(FBNCardButtonAction)action withOpenURL:(nullable NSURL *)url {
    [FBNCardAppEventsLogger logButtonAction:action forCardWithCampaignIdentifier:self.campaignIdentifier];

    id<FBNCardViewControllerDelegate> delegate = self.delegate;
    if (url) {
        if ([delegate respondsToSelector:@selector(pushCardViewController:willDismissWithOpenURL:)]) {
            [delegate pushCardViewController:self willDismissWithOpenURL:url];
        }
        [[UIApplication sharedApplication] openURL:url];
    } else {
        if ([delegate respondsToSelector:@selector(pushCardViewControllerWillDismiss:)]) {
            [delegate pushCardViewControllerWillDismiss:self];
        }
    }
    [self.assetsController clearAssetContentCacheForCardPayload:self.payload];

    [self dismissViewControllerAnimated:YES completion:nil];
}

///--------------------------------------
#pragma mark - FBNCardActionsView
///--------------------------------------

- (void)actionsView:(FBNCardActionsView *)view didPerformButtonAction:(FBNCardButtonAction)action withOpenURL:(nullable NSURL *)url {
    [self _dismissFromButtonAction:action withOpenURL:url];
}

///--------------------------------------
#pragma mark - Dismiss Button
///--------------------------------------

- (void)_dismissButtonAction {
    [self _dismissFromButtonAction:FBNCardButtonActionDismiss withOpenURL:nil];
}

@end
