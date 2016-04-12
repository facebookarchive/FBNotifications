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

#import "ViewController.h"

#import <FBNotifications/FBNotifications.h>

NS_ASSUME_NONNULL_BEGIN

static const CGFloat FBNotificationsExampleButtonHeight = 44.0;

@interface ViewController ()

@property (nonatomic, copy, readonly) NSArray<NSString *> *examplePaths;
@property (nullable, nonatomic, copy) NSArray<UIButton *> *buttons;

@end

@implementation ViewController

@synthesize examplePaths = _examplePaths;

///--------------------------------------
#pragma mark - View
///--------------------------------------

- (void)viewDidLoad {
    [super viewDidLoad];

    self.buttons = [self _buttonsArrayWithCount:self.examplePaths.count];
}

- (void)viewDidLayoutSubviews {
    [super viewDidLayoutSubviews];

    const CGRect bounds = self.view.bounds;

    CGFloat buttonsHeight = FBNotificationsExampleButtonHeight * self.buttons.count;
    CGFloat buttonY = CGRectGetMidY(bounds) - buttonsHeight / 2.0;
    for (UIButton *button in self.buttons) {
        CGRect buttonFrame = CGRectZero;
        buttonFrame.size.width = [button sizeThatFits:bounds.size].width;
        buttonFrame.size.height = FBNotificationsExampleButtonHeight;
        buttonFrame.origin.x = CGRectGetMidX(bounds) - CGRectGetMidX(buttonFrame);
        buttonFrame.origin.y = buttonY;
        button.frame = buttonFrame;

        buttonY = CGRectGetMaxY(buttonFrame);
    }
}

///--------------------------------------
#pragma mark - Buttons
///--------------------------------------

- (void)_buttonAction:(UIButton *)button {
    NSUInteger buttonIndex = [self.buttons indexOfObject:button];
    if (buttonIndex == NSNotFound) {
        return;
    }

    NSString *path = self.examplePaths[buttonIndex];
    NSData *data = [NSData dataWithContentsOfFile:path];
    NSDictionary *payload = [NSJSONSerialization JSONObjectWithData:data options:0 error:nil];
    if (!payload) {
        return;
    }

    //
    // Here is the most interesting part of this example.
    // We have 1 of 2 choices here - present the card immediately from the payload
    // or schedule a local notification and mimic the remote notification.
    //
    // We use the present immediately from remote to simply showcase what you can do with cards,
    // but the generic proper one should actually go through remote notification methods.
    //
    // Please note: Card presentation is not queued, so you can schedule presentation of more than a single card on a view controller.
    // Not queuing them and not waiting for animation to finish leads to undefined behavior, so please write a simple queue on top.

    //
    // Approach #1: Present immediately, as if you would get it from remote notification.
    [[FBNotificationsManager sharedManager] presentPushCardForRemoteNotificationPayload:payload
                                                                     fromViewController:self
                                                                             completion:nil];
    //
    // Approach #2: Mimic presentation via remote notification, by simply invoking a method.
//    UIApplication *application = [UIApplication sharedApplication];
//    [application.delegate application:application
//         didReceiveRemoteNotification:payload
//               fetchCompletionHandler:^(UIBackgroundFetchResult result) {}];
}

- (void)setButtons:(nullable NSArray<UIButton *> *)buttons {
    if (self.buttons != buttons) {
        [self.buttons makeObjectsPerformSelector:@selector(removeFromSuperview)];
        _buttons = buttons;
        for (UIButton *button in buttons) {
            [self.view addSubview:button];
        }
        [self.view setNeedsLayout];
    }
}

- (NSArray<UIButton *> *)_buttonsArrayWithCount:(NSUInteger)count {
    NSMutableArray *buttons = [NSMutableArray array];
    while (buttons.count != count) {
        UIButton *button = [UIButton buttonWithType:UIButtonTypeSystem];
        [button setTitle:[NSString stringWithFormat:@"Show Example #%lu", (unsigned long)buttons.count + 1] forState:UIControlStateNormal];
        [button addTarget:self action:@selector(_buttonAction:) forControlEvents:UIControlEventTouchUpInside];
        [buttons addObject:button];
    }
    return buttons;
}

///--------------------------------------
#pragma mark - Examples
///--------------------------------------

- (NSArray<NSString *> *)examplePaths {
    if (!_examplePaths) {
        NSBundle *bundle = [NSBundle mainBundle];
        NSMutableArray *array = [NSMutableArray array];
        [array addObject:[bundle pathForResource:@"example1" ofType:@"json"]];
        [array addObject:[bundle pathForResource:@"example2" ofType:@"json"]];
        [array addObject:[bundle pathForResource:@"example3" ofType:@"json"]];
        [array addObject:[bundle pathForResource:@"example4" ofType:@"json"]];
        [array addObject:[bundle pathForResource:@"example5" ofType:@"json"]];
        _examplePaths = [array copy];
    }
    return _examplePaths;
}

@end

NS_ASSUME_NONNULL_END
