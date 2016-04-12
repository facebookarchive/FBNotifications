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

#import "FBNCardActionButton.h"

#import "FBNCardActionConfiguration.h"
#import "FBNCardBackground.h"
#import "FBNCardTextContent.h"

@implementation FBNCardActionButton

- (instancetype)initWithConfiguration:(FBNCardActionConfiguration *)configuration
                         cornerRadius:(CGFloat)cornerRadius
                               action:(FBNCardButtonAction)action {
    self = [super initWithFrame:CGRectZero];
    if (!self) return self;

    _configuration = configuration;
    _action = action;

    self.clipsToBounds = YES;
    self.layer.cornerRadius = cornerRadius;

    [self setAttributedTitle:configuration.content.attributedText forState:UIControlStateNormal];

    if (configuration.backgroundColor) {
        [self setBackgroundImage:FBNCardBackgroundImageWithColor(configuration.backgroundColor) forState:UIControlStateNormal];
    }
    if (configuration.borderColor) {
        self.layer.borderColor = configuration.borderColor.CGColor;
        self.layer.borderWidth = configuration.borderWidth;
    }

    return self;
}

+ (instancetype)buttonFromConfiguration:(FBNCardActionConfiguration *)configuration
                       withCornerRadius:(CGFloat)cornerRadius
                                 action:(FBNCardButtonAction)action {
    return [[self alloc] initWithConfiguration:configuration cornerRadius:cornerRadius action:action];
}

@end
