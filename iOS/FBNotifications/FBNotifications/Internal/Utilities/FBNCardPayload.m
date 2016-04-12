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

#import "FBNCardPayload.h"

///--------------------------------------
#pragma mark - Version
///--------------------------------------

typedef struct {
    uint32_t major;
    uint32_t minor;
    uint32_t patch;
} FBNCardPayloadVersion;

static const FBNCardPayloadVersion FBNCardPayloadVersionInvalid = { .major = -1, .minor = -1, .patch = -1 };

BOOL FBNCardPayloadVersionIsValid(FBNCardPayloadVersion version) {
    return (version.major != FBNCardPayloadVersionInvalid.major &&
            version.minor != FBNCardPayloadVersionInvalid.minor &&
            version.patch != FBNCardPayloadVersionInvalid.patch);
}

FBNCardPayloadVersion FBNCardPayloadVersionFromString(NSString *string) {
    FBNCardPayloadVersion version = {0, 0, 0};
    int tokens = sscanf([string UTF8String], "%u.%u.%u", &version.major, &version.minor, &version.patch);
    if (tokens < 2) {
        return FBNCardPayloadVersionInvalid;
    }
    return version;
}

///--------------------------------------
#pragma mark - Public
///--------------------------------------

FBNCardPayload *_Nullable FBNCardPayloadFromRemoteNotificationPayload(NSDictionary *payload) {
    return payload[@"fb_push_card"];
}

BOOL FBNCardPayloadIsCompatibleWithCurrentVersion(FBNCardPayload *payload, NSString *frameworkVersionString) {
    FBNCardPayloadVersion payloadVersion = FBNCardPayloadVersionFromString(payload[@"version"]);
    FBNCardPayloadVersion frameworkVersion = FBNCardPayloadVersionFromString(frameworkVersionString);

    // Check for both versions to be valid
    if (!FBNCardPayloadVersionIsValid(payloadVersion) ||
        !FBNCardPayloadVersionIsValid(frameworkVersion)) {
        return NO;
    }
    // Not forward/backward compatible on major
    if (payloadVersion.major != frameworkVersion.major) {
        return NO;
    }
    // Not forward compatible on minor
    if (payloadVersion.minor > frameworkVersion.minor) {
        return NO;
    }
    // Don't care about patch
    return YES;
}

