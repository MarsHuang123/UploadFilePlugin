//
//  UploadFile.m
//  Hello
//
//  Created by mars on 06/12/2016.
//
//

#import "UploadFile.h"
#import "SoundQueueManager.h"

@interface UploadFile()<SoundQueueManagerDelegate>

@end

@implementation UploadFile

- (void)isRunning:(CDVInvokedUrlCommand*)command
{
    
    [self.commandDelegate runInBackground:^{
        SoundQueueManager *queue = [SoundQueueManager sharedInstance];
        int runing = [queue uploaderRunning];
        
        CDVPluginResult* result = [CDVPluginResult
                                   resultWithStatus:CDVCommandStatus_OK
                                   messageAsInt:runing];
        
        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    }];
    
}

- (void)dataPath:(CDVInvokedUrlCommand*)command
{
    
    
    [self.commandDelegate runInBackground:^{
        SoundQueueManager *queue = [SoundQueueManager sharedInstance];
        NSString *path = [queue dataPath];
        
        CDVPluginResult* result = [CDVPluginResult
                                   resultWithStatus:CDVCommandStatus_OK
                                   messageAsString:path];
        
        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    }];
}

- (void)resume:(CDVInvokedUrlCommand*)command
{
    
    
    [self.commandDelegate runInBackground:^{
        
        
        
        NSString* LoginID = [[command arguments] objectAtIndex:0];
        
        SoundQueueManager *queue = [SoundQueueManager sharedInstance];
        queue.delegate = self;
        [queue resumeWithLoginID:LoginID];
        
        int runing = [queue uploaderRunning];
        CDVPluginResult* result = [CDVPluginResult
                                   resultWithStatus:CDVCommandStatus_OK
                                   messageAsInt:runing];
        
        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    }];
}

#pragma mark - SoundQueueManagerDelegate
- (void)uploadFinishWithCaseID:(NSString *)pCaseID
{
    
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        
        dispatch_async(dispatch_get_main_queue(), ^{
            NSString *js = [NSString stringWithFormat:@"upload.uploadFinish('%@');", pCaseID];
            NSLog(@"%@", js);
//            [self.commandDelegate evalJs:js];

            [self.webView stringByEvaluatingJavaScriptFromString:js];
        });
        
    });
}

@end
