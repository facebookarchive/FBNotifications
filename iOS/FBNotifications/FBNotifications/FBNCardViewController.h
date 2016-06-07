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

NS_ASSUME_NONNULL_BEGIN

@class FBNCardViewController;

/**
 The `FBNCardViewControllerDelegate` defines methods for delegate of `FBNCardViewController`.
 */
@protocol FBNCardViewControllerDelegate <NSObject>

@optional

/**
 Called when a view controller is about to dismiss and open a URL.

 @param controller The view controller that called this method.
 @param url        URL that will be open after the view controller is dismissed.
 */
- (void)pushCardViewController:(FBNCardViewController *)controller willDismissWithOpenURL:(NSURL *)url;

/**
 Called when a view controller is about to dismiss without opening any URL.

 @param controller The view controller that called this method.
 */
- (void)pushCardViewControllerWillDismiss:(FBNCardViewController *)controller;

@end

/**
 `FBNCardViewController` is the main entry point for presenting a card that represents In-App Notification from a payload.
 This class encapsulates the display and layout of the entire push card and is intended to be presented full screen.
 
 It is invalid to initialize this class yourself, but it's intended to be used from `FBNotificationsManager`.
*/
@interface FBNCardViewController : UIViewController

/**
 The delegate of the card view controller.
 */
@property (nonatomic, weak) id<FBNCardViewControllerDelegate> delegate;

///--------------------------------------
#pragma mark - Unavailable Methods
///--------------------------------------

/**
 Allocates memory and initializes a new instance into it.

 @warning This method is unavaialble. Please use `FBNotificationsManager` to create the view controller.
 */
+ (instancetype)new NS_UNAVAILABLE;

/**
 Initializes a new instance.

 @warning This method is unavaialble. Please use `FBNotificationsManager` to create the view controller.
 */
- (instancetype)init NS_UNAVAILABLE;

/**
 Returns an object initiailized from data in a given unarchiver.

 @param decoder The unarchiver object.
 
 @warning This method is unavaialble. Please use `FBNotificationsManager` to create the view controller.
 */
- (nullable instancetype)initWithCoder:(NSCoder *)decoder NS_UNAVAILABLE;

/**
 Returns a newly initialized view controller with the nib file in the specified bundle.

 @param nibNameOrNil   The name of the nib file to associate with the view controller or `nil`.
 @param nibBundleOrNil he bundle in which to search for the nib file or `nil`.
 
 @warning This method is unavaialble. Please use `FBNotificationsManager` to create the view controller.
 */
- (instancetype)initWithNibName:(nullable NSString *)nibNameOrNil bundle:(nullable NSBundle *)nibBundleOrNil NS_UNAVAILABLE;

@end

NS_ASSUME_NONNULL_END
