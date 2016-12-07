//
//  UploadFile.h
//  Hello
//
//  Created by mars on 06/12/2016.
//
//
#import <Cordova/CDV.h>

//#import <Foundation/Foundation.h>

@interface UploadFile : CDVPlugin
- (void)isRunning:(CDVInvokedUrlCommand*)command;
- (void)dataPath:(CDVInvokedUrlCommand*)command;
- (void)resume:(CDVInvokedUrlCommand*)command;
@end
