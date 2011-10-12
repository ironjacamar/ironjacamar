#include <jni.h>
#include <string.h>
#include "build/c/HelloWorld.h"

/*
 * Class:     org_jboss_jca_samples_helloworld_HelloWorldManagedConnection
 * Method:    helloWorld
 * Signature: (Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL 
Java_org_jboss_jca_samples_helloworld_HelloWorldManagedConnection_helloWorld(JNIEnv *env, jobject o, jstring s)
{
   int length = 0;

   if (s != NULL)
      length = (*env)->GetStringLength(env, s);

   char *buf = (char*)malloc(16 + length);

   strcpy(buf, "Hello world, ");

   if (s != NULL)
      strcat(buf, (*env)->GetStringUTFChars(env, s, 0));

   strcat(buf, " !");

   jstring result = (*env)->NewStringUTF(env, buf);

   free(buf);

   return result;
}
