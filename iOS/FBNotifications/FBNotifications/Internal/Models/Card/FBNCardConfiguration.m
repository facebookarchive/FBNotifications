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

#import "FBNCardConfiguration.h"

#import "FBNAssetsController.h"
#import "FBNCardHeroConfiguration.h"
#import "FBNCardBodyConfiguration.h"
#import "FBNCardActionsConfiguration.h"

NS_ASSUME_NONNULL_BEGIN

@implementation FBNCardConfiguration

///--------------------------------------
#pragma mark - Init
///--------------------------------------

- (instancetype)initFromDictionary:(NSDictionary *)dictionary
                withDisplayOptions:(FBNCardDisplayOptions *)displayOptions
                 heroConfiguration:(FBNCardHeroConfiguration *)heroConfiguration
                 bodyConfiguration:(FBNCardBodyConfiguration *)bodyConfiguration
              actionsConfiguration:(FBNCardActionsConfiguration *)actionsConfiguration {
    self = [super init];
    if (!self) return self;

    _displayOptions = displayOptions;
    _heroConfiguration = heroConfiguration;
    _bodyConfiguration = bodyConfiguration;
    _actionsConfiguration = actionsConfiguration;

    return self;
}

+ (void)loadFromDictionary:(NSDictionary *)dictionary
        withDisplayOptions:(FBNCardDisplayOptions *)displayOptions
          assetsController:(FBNAssetsController *)controller
                completion:(void (^)(FBNCardConfiguration * _Nullable configuration))completion {
    dispatch_group_t group = dispatch_group_create();

    dispatch_group_enter(group);
    __block FBNCardHeroConfiguration *heroConfiguration = nil;
    [FBNCardHeroConfiguration loadFromDictionary:dictionary[@"hero"]
                                assetsController:controller
                                      completion:^(FBNCardHeroConfiguration * _Nullable configuration) {
                                          heroConfiguration = configuration;
                                          dispatch_group_leave(group);
                                      }];


    dispatch_group_enter(group);
    __block FBNCardBodyConfiguration *bodyConfiguration = nil;
    [FBNCardBodyConfiguration loadFromDictionary:dictionary[@"body"]
                                assetsController:controller
                                      completion:^(FBNCardBodyConfiguration * _Nullable configuration) {
                                          bodyConfiguration = configuration;
                                          dispatch_group_leave(group);
                                      }];

    dispatch_group_enter(group);
    __block FBNCardActionsConfiguration *actionsConfiguration = nil;
    [FBNCardActionsConfiguration loadFromDictionary:dictionary[@"actions"]
                                   assetsController:controller
                                         completion:^(FBNCardActionsConfiguration * _Nullable configuration) {
                                             actionsConfiguration = configuration;
                                             dispatch_group_leave(group);
                                         }];


    dispatch_group_notify(group, dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_HIGH, 0), ^{
        FBNCardConfiguration *configuration = [[FBNCardConfiguration alloc] initFromDictionary:dictionary
                                                                            withDisplayOptions:displayOptions
                                                                             heroConfiguration:heroConfiguration
                                                                             bodyConfiguration:bodyConfiguration
                                                                          actionsConfiguration:actionsConfiguration];
        completion(configuration);
    });
}

@end

NS_ASSUME_NONNULL_END
