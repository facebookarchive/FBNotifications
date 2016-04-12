# [1.0] Facebook In-App Notifications Format

## Types

We use basic JSON types to describe everything, since it's portable and supported on all platforms.

There are more complex types that form a JSON object by themselves (e.g. %CARD_BACKGROUND%) to eliminate ambiguity on the values as well as to be future compatible. When you see anything that starts and ends with a '%' - it means it's a custom type, that has separate piece of documentation in this document.

## Card Components

These components are attached to specific portions of content and are used only in a single place.

### %CARD_PAYLOAD%

Push Card consists of 3 basic parts: Hero, Body, Actions, which are going to be represented by JSON Objects.
Each one is going to be described in a separate section, in addition to the payload itself. 
This is a description for the basic payload, since it has few more things:

```
{
  "alert" : %ALERT%,
  "version" : "1.0", // "major.minor.patch", where 'patch' is optional.
  
  "size" : "small" // Values: "small", "medium", "large". Non-optional.
  "cornerRadius" : 2.0, // Default: 0
  "contentInset" : 5.0, // Default: 10.0
  
  "backdropColor" : "#ABABABFF", // RGBA Hex Value. Default: #00000000.
  "dismissColor" : "#ABABABFF", // RGBA Hex Value. Default: #000000FF.

  "hero" : %CARD_HERO%, // Optional
  "body" : %CARD_BODY%, // Optional
  "actions" : %CARD_ACTIONS% // Optional
}
```

### %ALERT%

This is used to specify the local system notification title/body.

```
{
  "title" : "Yarr!",
  "body" : "The quick brown fox jumps over the lazy dawg."
}
```

### %CARD_HERO%

```
{
  "height" : 0.5, // 0.0 to 1.0. Optional.
  "background" : %IMAGE%, // Values: %IMAGE%/%COLOR%/%GIF%
  "content" : %STYLED_TEXT%, // Optional.
  "contentAlign" : "center" // Values: "top"/"center"/"bottom". Default: "center".
}
```

### %CARD_BODY%

```
{
  "background" : %IMAGE%, // Values: %IMAGE% or %COLOR%
  "content" : %STYLED_TEXT% // Optional.
}
```

### %CARD_ACTIONS%

```
{
  "style" : "attached", // Values: "attached" or "detached"
  "layoutStyle" : "horizontal", // Values: "horizontal" or "vertical"
  
  "background" : %COLOR%, // Values: %IMAGE% or %COLOR%

  "height" : 50.0, // Default: 44.0
  "topInset" : 5.0, // Default: 0.0
  "contentInset" : 5.0, // Default: 0.0
  "cornerRadius": 0.0, // Default: 0.0
  
  "actions" : [ %CARD_ACTION% ] // Array of actions
}
```

### %CARD_ACTION%

```
{
  "backgroundColor" : "#ABABABFF", // RGBA Hex Value. Optional.
  "borderColor" : "#ABABABFF", // RGBA Hex Value. Optional.
  "borderWidth" : 2.0, // Default: 0.0
  "content" : %STYLED_TEXT%, // Optional
  "url" : "https://parse.com/" // Optional. Will dismiss card if none set.
}
```

## Basic Components

These components could be used in different types throughout the payload.

### %IMAGE%

```
{
  "_type" : "Image",
  "url" : "https://parse.com/favicon.ico"
}
```

### %COLOR%

```
{
  "_type" : "Color",
  "rgbaHex" : "#101010FF" // RGBA Hex Value
}
```

### %GIF%

```
{
  "_type" : "GIF",
  "url" : "https://parse.com/yolo.gif"
}  
```

### %STYLED_TEXT%

```
{
  "_type" : "StyledText",
  "align" : "left", // Values: "left" or "center" or "right".
  "size" : 15.0,
  "font" : %FONT%, // Optional. Default: System Font in Regular weight.
  "text" : "Hello World!",
  "color" : "#EDEDEDFF" // RGBA Hex Value
}
```

### %FONT%

A string representation of the font name with optional weight.
Built-in values that should be handled are:
```
"system-regular"
"system-light"
"system-bold"
"system-italic"
"system-bolditalic"
```

## Example Payload

```
{
  "fb_push_payload" : {
    "campaign" : "100500"
  },
  "fb_push_card": {
    "size": "medium",
    "cornerRadius": 30.0,
    "contentInset" : 20,
    "backdropColor": "#00000094",
    "hero": {
      "background": {
        "_type": "GIF",
        "url": "https://www.awwwards.com/awards/images/2011/07/gif-cinematic03.gif"
      },
      "content": {
        "_type": "StyledText",
        "size": 19,
        "text": "THE 🔑 IS TO HAVE EVERY 🔑",
        "align": "center",
        "textColor": "#FFFFFFFF"
      },
      "contentAlign" : "bottom",
      "height": 1.0
    },
    "body": {
      "background": {
        "_type": "Color",
        "rgbaHex": "#2B2B2BFF"
      },
      "content": {
        "_type": "StyledText",
        "size": 15,
        "text": "Hey username, we thought you'd be stoked to check out the new hottness from Drake. Rolling Stone has called this 'the song of the summer' and we're inclined to agree.",
        "align": "left",
        "color": "#FFFFFFFF"
      }
    },
    "alert" : {
      "title": "Bad Alert!",
      "body" : "Open me for details!"
    },
    "actions": {
      "style": "attached",
      "layoutStyle" : "horizontal", 
      "contentInset" : 0.0,
      "topInset": 0.0,
      "height" : 50.0,
      "actions": [
        {
          "backgroundColor": "#00D95AFF",
          "content": {
            "_type" : "StyledText"
            "text" : "Watch Now!",
            "color": "#FFFFFFFF"
          }
          "url": "https://parse.com/"
        },
        {
          "borderColor": "#294034FF",
          "borderWidth": 2,
          "content": {
            "_type" : "StyledText"
            "text" : "Cancel",
            "color": "#FFFFFFFF"
          }
        }
      ]
    }
  }
}
```

