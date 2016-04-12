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

#import "FBNCardActionsConfiguration.h"

#import "FBNAssetsController.h"
#import "FBNCardActionConfiguration.h"
#import "FBNCardViewUtilities.h"

@implementation FBNCardActionsConfiguration

///--------------------------------------
#pragma mark - Init
///--------------------------------------

- (instancetype)initFromDictionary:(nullable NSDictionary<NSString *, id> *)dictionary
                  assetsController:(FBNAssetsController *)controller {
    self = [super init];
    if (!self) return self;

    _style = FBNCardActionsStyleFromString(dictionary[@"style"]);
    _layoutStyle = FBNCardActionsLayoutStyleFromString(dictionary[@"layoutStyle"]);
    
    _background = [controller assetFromDictionary:dictionary[@"background"]];

    NSNumber *height = dictionary[@"height"] ?: @(44.0); // Defaults to 44.0
    _height = FBNCGFloatFromNumber(height);
    _topInset = FBNCGFloatFromNumber(dictionary[@"topInset"]); // Default to 0
    _contentInset = FBNCGFloatFromNumber(dictionary[@"contentInset"]); // Defaults to 0
    _cornerRadius = FBNCGFloatFromNumber(dictionary[@"cornerRadius"]); // Defaults to 0

    NSArray<NSDictionary *> *rawActions = dictionary[@"actions"];
    NSMutableArray<FBNCardActionConfiguration *> *actions = [NSMutableArray arrayWithCapacity:rawActions.count];
    for (NSDictionary *rawAction in rawActions) {
        FBNCardActionConfiguration *action = [FBNCardActionConfiguration configurationFromDictionary:rawAction];
        [actions addObject:action];
    }
    _actions = [actions copy];

    return self;
}

+ (nullable instancetype)configurationFromDictionary:(nullable NSDictionary<NSString *, id> *)dictionary
                                    assetsController:(FBNAssetsController *)controller {
    if (!dictionary) {
        return nil;
    }
    return [[self alloc] initFromDictionary:dictionary assetsController:controller];
}

@end
