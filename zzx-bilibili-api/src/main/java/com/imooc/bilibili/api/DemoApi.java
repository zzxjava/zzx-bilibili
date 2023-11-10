package com.imooc.bilibili.api;

import com.imooc.bilibili.service.DemoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;



@RestController
public class DemoApi {
    @Autowired
    DemoService demoService;

    @GetMapping("/query")
    public Long query(Long id) {
        return demoService.query(id);
    }

    public static void swap(int[] arr,int min,int i){
        int tmp = arr[min];
        arr[min] = arr[i];
        arr[i] = tmp;
    }


    // 双链表节点
    public static class DoubleListNode {
        public int value;
        public DoubleListNode last;
        public DoubleListNode next;

        public DoubleListNode(int v) {
            value = v;
        }
    }



    /**
     * 反转双链表
     * */
    public static DoubleListNode reverseDoubleList(DoubleListNode head){
        DoubleListNode pre = null;
        DoubleListNode next = null;

        while (head != null){
            next = head.next;
            head.next = pre;
            head.last = next;//前节点也要换
            pre = head;
            head = next;
        }
        return pre;
    }



    public static void main(String[] args) {
        DoubleListNode node1 = new DoubleListNode(0);
        DoubleListNode node2 = new DoubleListNode(1);
        DoubleListNode node3 = new DoubleListNode(2);
        DoubleListNode node4 = new DoubleListNode(3);
        DoubleListNode node5 = new DoubleListNode(4);

        node1.last = null;
        node1.next = node2;
        node2.last = node1;
        node2.next = node3;
        node3.last = node2;
        node3.next = node4;
        node4.last = node3;
        node4.next = node5;
        node5.last = node4;
        node5.next = null;

        DoubleListNode current = node1;
        // 打印反转前的链表
        while (current != null){
            System.out.print(current.value+" ");
            current = current.next;
        }
        System.out.println();
        System.out.println("==================");
        // 打印反转后的链表
        DoubleListNode newHead = reverseDoubleList(node1);
        while (newHead != null){
            System.out.print(newHead.value+" ");
            newHead = newHead.next;
        }

    }




}

