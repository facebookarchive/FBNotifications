![FBNotifications logo](.github/FBNotifications-Logo.png?raw=true)

![Platforms][platforms-svg]
[![Build Status][build-status-svg]][build-status-link]

Facebook In-App Notifications enables you to create rich and customizable in-app notifications and deliver them via push notifications, based on the actions people take in your app. You can use text, photos, animated GIFs, buttons or extend the open format to suit your needs.

## Getting Started on iOS

[![Podspec][podspec-svg]][podspec-link]
[![Carthage compatible][carthage-svg]](carthage-link)

To get started on iOS, install the framework using one of these options:

- **[CocoaPods](https://cocoapods.org)**

 Add the following line to your Podfile:
 ```ruby
 pod 'FBNotifications'
 ```
 Run `pod install`, and you should now have the latest framework installed.

- **[Carthage](https://github.com/carthage/carthage)**

 Add the following line to your Cartfile:
 ```
 github "facebook/FBNotifications"
 ```
 Run `carthage update`, and you should now have the latest version of the framework in your Carthage folder.

After you've installed the framework, you would need to add the following to your application delegate to present the notification:

Using *Objective-C*:

```objc
/// Present In-App Notification from remote notification (if present).
- (void)application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)userInfo fetchCompletionHandler:(nonnull void (^)(UIBackgroundFetchResult))completionHandler {
  FBNotificationsManager *notificationsManager = [FBNotificationsManager sharedManager];
  [notificationsManager presentPushCardForRemoteNotificationPayload:userInfo
                                                 fromViewController:nil
                                                         completion:^(FBNCardViewController * _Nullable viewController, NSError * _Nullable error) {
                                                           if (error) {
                                                             completionHandler(UIBackgroundFetchResultFailed);
                                                           } else {
                                                             completionHandler(UIBackgroundFetchResultNewData);
                                                           }
                                                         }];
}
```

Using *Swift*:
```swift
/// Present In-App Notification from remote notification (if present).
func application(application: UIApplication, didReceiveRemoteNotification userInfo: [NSObject : AnyObject], fetchCompletionHandler completionHandler: (UIBackgroundFetchResult) -> Void) {
  FBNotificationsManager.sharedManager().presentPushCardForRemoteNotificationPayload(userInfo, fromViewController: nil) { viewController, error in
    if let _ error = error {
      completionHandler(.Failed)
    } else {
      completionHandler(.NewData)
    }
  }
}
```

## Getting Started on Android

[![Maven Central][maven-svg]][maven-link]

To get started on Android, add the following to your gradle dependencies:

```gradle
compile 'com.facebook.android:notifications:1.+'
```

After you've added the dependency, you'll have to [set up a GCM listener service](https://developers.google.com/cloud-messaging/android/client), and add the following to your service:

```java
@Override
public void onMessageReceived(String from, final Bundle data) {
    NotificationsManager.presentNotification(
        this,
        data,
        new Intent(getApplicationContext(), MainActivity.class)
    );
}
```

Then when all the content for the notification is ready - it will automatically present the notification to the end user with a pending intent to present a card on open.
To hand-off the necessary data from the intent - you need to handle the notification in the `onCreate` function of your Main Activity:

```java
public class MainActivity extends AppCompatActivity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    NotificationsManager.presentCardFromNotification(this);
  }
}
```

For more help on getting started, take a look at our [Facebook Analytics Push Documentation](https://developers.facebook.com/docs/analytics/push-campaigns/android).

## In-App Notifications Format

In-app notifications are powered by a custom format that has an open specification available in this repository.
The format describes all the possible values and combinations of content that can be rendered by the framework.

We are open to accepting contributions to the format and the format is constantly evolving.
Any version of the framework is compatible with any previous version of the format in the same major version scope.

For example:
  - Framework `1.0.0` **is** compatible with format version `1.0`
  - Framework `1.0.0` **is not** compatible with format version `1.5`
  - Framework `1.5.0` **is** compatible with format version `1.5`
  - Framework `1.5.1` **is** compatible with format version `1.5`
  - Framework `2.0.0` **is not** compatible with format version `1.0`, or `1.5`

## Contributing

We want to make contributing to this project as easy and transparent as possible. Please refer to the [Contribution Guidelines](https://github.com/facebook/FBNotifications/blob/master/CONTRIBUTING.md).

## License

See the `LICENSE` file for source code.
See the `LICENSE-specification` file for In-App Notifications format specification.

 [platforms-svg]: https://img.shields.io/badge/platform-iOS%20%7C%20Android-lightgrey.svg

 [build-status-svg]: https://img.shields.io/travis/facebook/FBNotifications/master.svg
 [build-status-link]: https://travis-ci.org/facebook/FBNotifications/branches

 [podspec-svg]: https://img.shields.io/cocoapods/v/FBNotifications.svg
 [podspec-link]: https://cocoapods.org/pods/FBNotifications

 [carthage-svg]: https://img.shields.io/badge/Carthage-compatible-4BC51D.svg?style=flat
 [carthage-link]: https://github.com/carthage/carthage

 [maven-svg]: https://maven-badges.herokuapp.com/maven-central/com.facebook.android/notifications/badge.svg?style=flat
 [maven-link]: https://maven-badges.herokuapp.com/maven-central/com.facebook.android/notifications
