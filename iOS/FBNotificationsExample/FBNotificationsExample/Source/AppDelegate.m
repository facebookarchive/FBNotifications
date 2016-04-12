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

#import "AppDelegate.h"

#import <FBNotifications/FBNotifications.h>

@interface AppDelegate ()

@end

@implementation AppDelegate

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(nullable NSDictionary *)launchOptions {
    UIUserNotificationSettings *settings = [UIUserNotificationSettings settingsForTypes:UIUserNotificationTypeAlert categories:nil];
    [[UIApplication sharedApplication] registerUserNotificationSettings:settings];

    [[UIApplication sharedApplication] registerForRemoteNotifications];

    return YES;
}

- (void)application:(UIApplication *)application didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken {
    // TODO: Integrate with FBSDKCoreKit
}

- (void)application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)userInfo fetchCompletionHandler:(nonnull void (^)(UIBackgroundFetchResult))completionHandler {
    FBNotificationsManager *notificationsManager = [FBNotificationsManager sharedManager];
    if ([UIApplication sharedApplication].applicationState == UIApplicationStateBackground) {
        [notificationsManager preparePushCardContentForRemoteNotificationPayload:userInfo
                                                                      completion:^(NSDictionary * _Nullable payload, NSError * _Nullable error) {
                                                                          if (error) {
                                                                              completionHandler(UIBackgroundFetchResultFailed);
                                                                          } else {
                                                                              completionHandler(UIBackgroundFetchResultNewData);
                                                                          }
                                                                      }];
    } else {
        [notificationsManager presentPushCardForRemoteNotificationPayload:userInfo
                                                       fromViewController:nil
                                                               completion:^(FBNCardViewController * _Nullable viewController, NSError * _Nullable error) {
                                                                   if (error) {
                                                                       completionHandler(UIBackgroundFetchResultFailed);
                                                                   } else {
                                                                       completionHandler(UIBackgroundFetchResultNewData);
                                                                   }
                                                               }];
    }
}

@end
