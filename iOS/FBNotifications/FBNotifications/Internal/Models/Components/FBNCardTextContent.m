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

#import "FBNCardTextContent.h"

#import "FBNCardColor.h"
#import "FBNCardTextAlignment.h"
#import "FBNCardViewUtilities.h"
#import "FBNCardFont.h"

FBNCardContentVerticalAlignment FBNCardContentVerticalAlignmentFromString(NSString *_Nullable string) {
    if ([string isEqualToString:@"top"]) {
        return FBNCardContentVerticalAlignmentTop;
    } else if ([string isEqualToString:@"center"]) {
        return FBNCardContentVerticalAlignmentCenter;
    } else if ([string isEqualToString:@"bottom"]) {
        return FBNCardContentVerticalAlignmentBottom;
    }
    return FBNCardContentVerticalAlignmentCenter;
}

@implementation FBNCardTextContent

///--------------------------------------
#pragma mark - Init
///--------------------------------------

- (instancetype)initFromDictionary:(NSDictionary<NSString *, id> *)dictionary {
    self = [super init];
    if (!self) return self;

    NSString *fontName = dictionary[@"font"];
    NSNumber *fontSize = dictionary[@"size"] ?: @([UIFont systemFontSize]);
    _font = FBNCardFontWithNameSize(fontName, FBNCGFloatFromNumber(fontSize));
    _textColor = FBNCardColorFromRGBAHex(dictionary[@"color"]) ?: [UIColor blackColor]; // Defaults to Black
    _textAlignment = FBNCardTextAlignmentFromString(dictionary[@"align"]);

    NSMutableParagraphStyle *paragraphStyle = [[NSParagraphStyle defaultParagraphStyle] mutableCopy];
    paragraphStyle.alignment = _textAlignment;
    NSDictionary<NSString *, id> *attributes = @{ NSFontAttributeName : self.font,
                                                  NSForegroundColorAttributeName : self.textColor,
                                                  NSParagraphStyleAttributeName : paragraphStyle };
    
    _attributedText = [[NSAttributedString alloc] initWithString:dictionary[@"text"] attributes:attributes];

    return self;
}

+ (nullable instancetype)contentFromDictionary:(nullable NSDictionary<NSString *, id> *)dictionary {
    if (!dictionary) {
        return nil;
    }
    return [[self alloc] initFromDictionary:dictionary];
}

@end
