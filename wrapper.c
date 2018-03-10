#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include "PredictWrapper.h"
#include "model/model.h"

JNIEXPORT jint JNICALL Java_PredictWrapper_GetNumFeature
  (JNIEnv* env, jobject obj) {
  return (jint)get_num_feature();
}

JNIEXPORT jfloat JNICALL Java_PredictWrapper_Predict
  (JNIEnv* env, jobject obj, jbooleanArray bitmap,
  jfloatArray data, jboolean pred_margin) {
  
  jsize i;
  jsize len = (*env)->GetArrayLength(env, data);
  jboolean* bitmap_c = (*env)->GetBooleanArrayElements(env, bitmap, 0);
  jfloat* data_c = (*env)->GetFloatArrayElements(env, data, 0);

  union Entry* inst = (union Entry*)malloc(len * sizeof(union Entry));
  for (i = 0; i < len; ++i) {
    if (bitmap_c[i]) {
      inst[i].fvalue = (float)data_c[i];
    } else {
      inst[i].missing = -1;
    }
  }

  float result = predict_margin(inst);
  if (!pred_margin) {
    pred_transform(&result);
  }
  free(inst);

  (*env)->ReleaseFloatArrayElements(env, data, data_c, 0);
  (*env)->ReleaseBooleanArrayElements(env, bitmap, bitmap_c, 0);
  return (jfloat)result;
}
