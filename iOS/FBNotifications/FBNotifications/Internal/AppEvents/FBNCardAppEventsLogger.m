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

#import "FBNCardAppEventsLogger.h"

NS_ASSUME_NONNULL_BEGIN

@interface FBSDKAppEvents : NSObject

+ (void)logEvent:(NSString *)eventName parameters:(nullable NSDictionary<NSString *, NSString *> *)parameters;

@end

@implementation FBNCardAppEventsLogger

///--------------------------------------
#pragma mark - Public
///--------------------------------------

+ (nullable NSString *)campaignIdentifierFromRemoteNotificationPayload:(NSDictionary *)payload {
    return payload[@"fb_push_payload"][@"campaign"];
}

+ (void)logCardOpenWithCampaignIdentifier:(nullable NSString *)identifier {
    if (!identifier) {
        return;
    }
    [self _logAppEventWithName:@"fb_mobile_push_opened" campaignIdentifier:identifier];
}

+ (void)logButtonAction:(FBNCardButtonAction)action forCardWithCampaignIdentifier:(nullable NSString *)identifier {
    NSString *eventName = [self _appEventNameForButtonAction:action];
    [self _logAppEventWithName:eventName campaignIdentifier:identifier];
}

///--------------------------------------
#pragma mark - Private
///--------------------------------------

+ (void)_logAppEventWithName:(NSString *)name campaignIdentifier:(NSString *)identifier {
    if (!identifier) {
        return;
    }

    Class loggerClass = NSClassFromString(@"FBSDKAppEvents");
    if (loggerClass && [loggerClass respondsToSelector:@selector(logEvent:parameters:)]) {
        [loggerClass logEvent:name
                   parameters:@{ @"fb_push_campaign" : identifier }];
    }
}

+ (nullable NSString *)_appEventNameForButtonAction:(FBNCardButtonAction)action {
    switch (action) {
        case FBNCardButtonActionPrimary:
            return @"fb_mobile_push_card_action_primary";
        case FBNCardButtonActionSecondary:
            return @"fb_mobile_push_card_action_secondary";
        case FBNCardButtonActionDismiss:
            return @"fb_mobile_push_card_action_dismiss";
        default:break;
    }
    return nil;
}

@end

NS_ASSUME_NONNULL_END
