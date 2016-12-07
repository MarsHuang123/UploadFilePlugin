//
//  UploadFile.m
//  Hello
//
//  Created by mars on 06/12/2016.
//
//

#import "UploadFile.h"
#import "SoundQueueManager.h"

@implementation UploadFile

- (void)isRunning:(CDVInvokedUrlCommand*)command
{
    
    SoundQueueManager *queue = [SoundQueueManager sharedInstance];
    int runing = [queue uploaderRunning];
    
    CDVPluginResult* result = [CDVPluginResult
                               resultWithStatus:CDVCommandStatus_OK
                               messageAsInt:runing];
    
    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}

- (void)dataPath:(CDVInvokedUrlCommand*)command
{
    
    SoundQueueManager *queue = [SoundQueueManager sharedInstance];
    NSString *path = [queue dataPath];
    
    CDVPluginResult* result = [CDVPluginResult
                               resultWithStatus:CDVCommandStatus_OK
                               messageAsString:path];
    
    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}

- (void)resume:(CDVInvokedUrlCommand*)command
{
    
    NSString* LoginID = [[command arguments] objectAtIndex:0];
    
    SoundQueueManager *queue = [SoundQueueManager sharedInstance];
    [queue resumeWithLoginID:LoginID];
    
    int runing = [queue uploaderRunning];
    CDVPluginResult* result = [CDVPluginResult
                               resultWithStatus:CDVCommandStatus_OK
                               messageAsInt:runing];
    
    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}

@end
