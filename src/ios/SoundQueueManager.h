//
//  SoundQueueManager.h
//  AFSoundManager-Demo
//
//  Created by mars on 16/7/7.
//  Copyright © 2016年 AlvaroFranco. All rights reserved.
//

#import <Foundation/Foundation.h>


typedef NS_ENUM(NSInteger, TaskStatus)  {
    TaskStatusNoUpload,
    TaskStatusUploading,
    TaskStatusUploadFailed,
    TaskStatusUploadFinished
};

typedef NS_ENUM(NSInteger, SessionStatus) {
    SessionStatusAvailable = 0,
    SessionStatusBusy = 1
};

@class UploadTask;

@interface SoundQueueManager : NSObject

+ (SoundQueueManager *)sharedInstance;

- (BOOL)uploaderRunning;
- (NSString *)dataPath;
- (void)resumeWithLoginID:(NSString *)pLogninID;

@end

@interface Uploader : NSObject

+ (Uploader *)sharedInstance;

- (void)addTaskToQueueWithUploadTask:(NSString *)pUploadTask
                     completionBlock:(void(^)(TaskStatus taskStatus))pCompletionBlock;
- (SessionStatus)sessionStatus;
- (void)setLoginID:(NSString *)pLoginID;
- (void)setSessionStatusWithStatus:(SessionStatus)pSessionStatus;

@end

#pragma mark - categories

@interface NSMutableArray (Queue)
- (id)dequeue;
- (void)enqueue:(id)obj;
@end

void *NewBase64Decode(
                      const char *inputBuffer,
                      size_t length,
                      size_t *outputLength);

char *NewBase64Encode(
                      const void *inputBuffer,
                      size_t length,
                      bool separateLines,
                      size_t *outputLength);

@interface NSData (Split)

/** Splits the source data into any array of components separated by the specified byte.
 
 Taken from http://www.geektheory.ca/blog/splitting-nsdata-object-data-specific-byte/
 
 @param sep Byte to separate by.
 @return NSArray of components
 */
- (NSMutableArray *)componentsSeparatedByChunkSize:(NSUInteger)pChunkSize;
- (NSData *)componentsSeparatedFromByte:(NSInteger)sep;
- (NSString *)base64EncodedString;

@end




