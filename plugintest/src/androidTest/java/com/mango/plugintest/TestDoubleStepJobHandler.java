package com.mango.plugintest;

import com.mango.puppetmodel.Job;

/**
 * TestHandler
 *
 * @author: hehongzhen
 * @date: 2020/07/23
 */
interface TestDoubleStepJobHandler {

    void onFirstStepSuccess(Job job);

    void onSecondStepSuccess(Job job);
}
