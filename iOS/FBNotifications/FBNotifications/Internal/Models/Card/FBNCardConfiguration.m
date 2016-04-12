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
#import "FBNCardColor.h"
#import "FBNCardHeroConfiguration.h"
#import "FBNCardBodyConfiguration.h"
#import "FBNCardActionsConfiguration.h"
#import "FBNCardViewUtilities.h"

NS_ASSUME_NONNULL_BEGIN

@implementation FBNCardConfiguration

///--------------------------------------
#pragma mark - Init
///--------------------------------------

- (instancetype)initFromDictionary:(NSDictionary *)dictionary
                  assetsController:(FBNAssetsController *)assetsController {
    self = [super init];
    if (!self) return self;

    _size = FBNCardSizeFromString(dictionary[@"size"]);
    _cornerRadius = FBNCGFloatFromNumber(dictionary[@"cornerRadius"]); // Defaults to 0
    _contentInset = FBNCGFloatFromNumber(dictionary[@"contentInset"] ?: @(10.0)); // Defaults to 10.0

    _backdropColor = FBNCardColorFromRGBAHex(dictionary[@"backdropColor"]);
    _dismissButtonColor = FBNCardColorFromRGBAHex(dictionary[@"dismissColor"]) ?: [UIColor blackColor];

    _heroConfiguration = [FBNCardHeroConfiguration configurationFromDictionary:dictionary[@"hero"] assetsController:assetsController];
    _bodyConfiguration = [FBNCardBodyConfiguration configurationFromDictionary:dictionary[@"body"] assetsController:assetsController];
    _actionsConfiguration = [FBNCardActionsConfiguration configurationFromDictionary:dictionary[@"actions"] assetsController:assetsController];

    return self;
}

+ (instancetype)configurationFromDictionary:(NSDictionary *)payload
                           assetsController:(FBNAssetsController *)assetsController {
    return [[self alloc] initFromDictionary:payload assetsController:assetsController];
}

@end

NS_ASSUME_NONNULL_END
