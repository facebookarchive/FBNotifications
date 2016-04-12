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

#import "FBNotificationsManager.h"

#import "FBNConstants.h"
#import "FBNCardError.h"
#import "FBNAssetContentCache.h"
#import "FBNCardViewUtilities.h"
#import "FBNCardViewController_Internal.h"
#import "FBNCardPayload.h"
#import "FBNCardAppEventsLogger.h"
#import "FBNAssetsController.h"
#import "FBNColorAsset.h"
#import "FBNColorAssetController.h"
#import "FBNImageAsset.h"
#import "FBNImageAssetController.h"
#import "FBNGIFAsset.h"
#import "FBNGIFAssetController.h"

@interface FBNotificationsManager ()

@property (nonnull, nonatomic, strong, readonly) FBNAssetsController *assetsController;

@property (nullable, nonatomic, weak) FBNCardViewController *currentCardViewController;

@end

@implementation FBNotificationsManager

///--------------------------------------
#pragma mark - Creating a Card Manager
///--------------------------------------

- (instancetype)init {
    self = [super init];
    if (!self) return self;

    _assetsController = [self _defaultConfigurationAssetsController];

    return self;
}

+ (instancetype)sharedManager {
    static FBNotificationsManager *manager;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        manager = [[self alloc] init];
    });
    return manager;
}

///--------------------------------------
#pragma mark - Assets Controller
///--------------------------------------

- (FBNAssetsController *)_defaultConfigurationAssetsController {
    FBNAssetsController *controller = [[FBNAssetsController alloc] init];
    [controller registerAssetController:[[FBNColorAssetController alloc] init] forAssetType:FBNColorAssetType];
    [controller registerAssetController:[[FBNImageAssetController alloc] init] forAssetType:FBNImageAssetType];
    [controller registerAssetController:[[FBNGIFAssetController alloc] init] forAssetType:FBNGIFAssetType];
    return controller;
}

///--------------------------------------
#pragma mark - Present from Remote Notification
///--------------------------------------

- (void)preparePushCardContentForRemoteNotificationPayload:(NSDictionary *)payload
                                                completion:(nullable FBNCardContentPreparationCompletion)completion {
    if (![self canPresentPushCardFromRemoteNotificationPayload:payload]) {
        if (completion) {
            completion(nil, [FBNCardError invalidRemoteNotificationPayloadError]);
        }
        return;
    }
    [self.assetsController cacheAssetContentForCardPayload:payload completion:^{
        if (completion) {
            completion(payload, nil);
        }
    }];
}

- (void)presentPushCardForRemoteNotificationPayload:(NSDictionary *)payload
                                 fromViewController:(nullable UIViewController *)viewController
                                         completion:(nullable FBNCardPresentationCompletion)completion {
    if (![self canPresentPushCardFromRemoteNotificationPayload:payload]) {
        if (completion) {
            completion(nil, [FBNCardError invalidRemoteNotificationPayloadError]);
        }
        return;
    }

    FBNCardPayload *cardPayload = FBNCardPayloadFromRemoteNotificationPayload(payload);
    NSString *campaignIdentifier = [FBNCardAppEventsLogger campaignIdentifierFromRemoteNotificationPayload:payload];

    FBNCardViewController *oldCardViewController = self.currentCardViewController;
    // Has the same payload and is visible on screen, meaning that we don't need to re-present it.
    if ([oldCardViewController.payload isEqual:cardPayload] &&
        oldCardViewController.presentingViewController != nil &&
        !oldCardViewController.isBeingDismissed) {
        if (completion) {
            completion(oldCardViewController, nil);
        }
        return;
    }

    // Dismiss the old one.
    [oldCardViewController dismissViewControllerAnimated:NO completion:nil];

    // Create and present the new one.
    FBNCardViewController *cardViewController = [[FBNCardViewController alloc] initWithPushCardPayload:cardPayload
                                                                                    campaignIdentifier:campaignIdentifier
                                                                                      assetsController:self.assetsController];
    viewController = viewController ?: FBNApplicationTopMostViewController();
    BOOL animated = (oldCardViewController == nil);
    [viewController presentViewController:cardViewController animated:animated completion:^{
        if (completion) {
            completion(cardViewController, nil);
        }
    }];
    // Save the new one into a weak property.
    self.currentCardViewController = cardViewController;
}

- (BOOL)canPresentPushCardFromRemoteNotificationPayload:(nullable NSDictionary *)payload {
    FBNCardPayload *cardPayload = FBNCardPayloadFromRemoteNotificationPayload(payload);
    return (cardPayload != nil && FBNCardPayloadIsCompatibleWithCurrentVersion(cardPayload, FBNotificationsCardFormatVersionString));
}

///--------------------------------------
#pragma mark - Present via Local Notification
///--------------------------------------

- (void)createLocalNotificationFromRemoteNotificationPayload:(NSDictionary *)payload
                                                  completion:(FBNLocalNotificationCreationCompletion)completion {
    if (![self canPresentPushCardFromRemoteNotificationPayload:payload]) {
        NSError *error = [FBNCardError invalidRemoteNotificationPayloadError];
        completion(nil, error);
        return;
    }

    FBNCardPayload *cardPayload = FBNCardPayloadFromRemoteNotificationPayload(payload);
    [self.assetsController cacheAssetContentForCardPayload:cardPayload completion:^{
        UILocalNotification *notification = [self _localNotificationFromPayload:cardPayload];
        notification.userInfo = payload;
        completion(notification, nil);
    }];
}

- (void)presentPushCardForLocalNotification:(UILocalNotification *)notification
                         fromViewController:(nullable UIViewController *)viewController
                                 completion:(nullable FBNCardPresentationCompletion)completion {
    [[UIApplication sharedApplication] cancelLocalNotification:notification];
    [self presentPushCardForRemoteNotificationPayload:notification.userInfo
                                   fromViewController:viewController
                                           completion:completion];
}

- (BOOL)canPresentPushCardFromLocalNotification:(UILocalNotification *)notification {
    return [self canPresentPushCardFromRemoteNotificationPayload:notification.userInfo];
}

- (nullable UILocalNotification *)_localNotificationFromPayload:(NSDictionary<NSString *, id> *)payload {
    UILocalNotification *notification = [[UILocalNotification alloc] init];
    notification.alertTitle = payload[@"alert"][@"title"];
    notification.alertBody = payload[@"alert"][@"body"];
    return notification;
}

@end
