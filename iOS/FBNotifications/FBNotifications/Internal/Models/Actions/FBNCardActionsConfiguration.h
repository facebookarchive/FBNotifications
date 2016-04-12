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

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

#import "FBNCardActionsStyle.h"

@class FBNAssetsController;
@class FBNCardActionConfiguration;
@protocol FBNAsset;

NS_ASSUME_NONNULL_BEGIN

@interface FBNCardActionsConfiguration : NSObject

///--------------------------------------
#pragma mark - Properties
///--------------------------------------

@property (nonatomic, assign, readonly) FBNCardActionsStyle style;
@property (nonatomic, assign, readonly) FBNCardActionsLayoutStyle layoutStyle;

@property (nullable, nonatomic, strong, readonly) id<FBNAsset> background;

@property (nonatomic, assign, readonly) CGFloat height;
@property (nonatomic, assign, readonly) CGFloat topInset;
@property (nonatomic, assign, readonly) CGFloat contentInset;
@property (nonatomic, assign, readonly) CGFloat cornerRadius;

@property (nullable, nonatomic, copy, readonly) NSArray<FBNCardActionConfiguration *> *actions;

///--------------------------------------
#pragma mark - Init
///--------------------------------------

- (instancetype)init NS_UNAVAILABLE;
+ (instancetype)new NS_UNAVAILABLE;

+ (nullable instancetype)configurationFromDictionary:(nullable NSDictionary<NSString *, id> *)dictionary
                                    assetsController:(FBNAssetsController *)controller;

@end

NS_ASSUME_NONNULL_END
