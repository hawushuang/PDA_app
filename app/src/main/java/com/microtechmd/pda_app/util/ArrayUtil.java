package com.microtechmd.pda_app.util;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Administrator on 2018/4/24.
 */

public class ArrayUtil {
    public static float getAverageNoZero(float[] array) {
        int null_count = 0;
        float sum = 0;
        for (float v : array) {
            sum += v;
            if (v == 0) {
                null_count++;
            }
        }
        return sum / (array.length - null_count);
    }

    public static float getAverage(float[] array) {
        float sum = 0;
        for (float v : array) {
            sum += v;
        }
        return sum / array.length;
    }

    //数组去0
    public static float[] getArrayDelZero(float[] array) {
        int count = 0;    //定义一个变量记录0的个数
        for (float anArray1 : array) {
            if (anArray1 == 0) {
                count++;
            }
        }
        //创建一个新的数组
        float[] newArr = new float[array.length - count];

        int index = 0; //新数组使用的索引值
        //把非的数据存储到新数组中。
        for (float anArray : array) {
            if (anArray != 0) {
                newArr[index] = anArray;
                index++;
            }
        }
        return newArr;
    }

    //求标准差
    public static float getStandardDeviation(float[] array) {
        float average = getAverageNoZero(array);  //求出数组的平均数
        int total = 0;
        for (float anArray : array) {
            total += Math.pow((anArray - average), 2);   //求出方差
        }
        return (float) Math.sqrt(total / array.length);//求出标准差
    }
}
