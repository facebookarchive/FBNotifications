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

#import "FBNCardSize.h"

@class FBNAssetsController;
@class FBNCardHeroConfiguration;
@class FBNCardBodyConfiguration;
@class FBNCardActionsConfiguration;

NS_ASSUME_NONNULL_BEGIN

@interface FBNCardConfiguration : NSObject

@property (nonatomic, assign, readonly) FBNCardSize size;
@property (nonatomic, assign, readonly) CGFloat cornerRadius;
@property (nonatomic, assign, readonly) CGFloat contentInset;

@property (nullable, nonatomic, strong, readonly) UIColor *backdropColor;
@property (nonatomic, strong, readonly) UIColor *dismissButtonColor;

@property (nullable, nonatomic, strong, readonly) FBNCardHeroConfiguration *heroConfiguration;
@property (nullable, nonatomic, strong, readonly) FBNCardBodyConfiguration *bodyConfiguration;
@property (nullable, nonatomic, strong, readonly) FBNCardActionsConfiguration *actionsConfiguration;

+ (instancetype)configurationFromDictionary:(NSDictionary *)payload
                           assetsController:(FBNAssetsController *)assetsController;

@end

NS_ASSUME_NONNULL_END
