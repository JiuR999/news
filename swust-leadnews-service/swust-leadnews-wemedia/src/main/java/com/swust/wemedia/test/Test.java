package com.swust.wemedia.test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Test {
    public static void main(String[] args) {
        // 生产者，可以指定返回结果
        CompletableFuture<String> firstTask = CompletableFuture.supplyAsync(() -> {
            System.out.println("异步任务开始执行");
            System.out.println("异步任务执行结束");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return "返回结果";
        });

        CompletableFuture<String> firstTask2 = CompletableFuture.supplyAsync(() -> {
            System.out.println("异步任务2开始执行");
            System.out.println("异步任务2执行结束");
            return "返回结果2";
        });

        String result1 = firstTask.join();
        String result2 = null;
        try {
            result2 = firstTask2.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        System.out.println(result1 + "," + result2);
    }
}
