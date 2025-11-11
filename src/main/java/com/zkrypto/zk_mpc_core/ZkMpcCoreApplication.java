package com.zkrypto.zk_mpc_core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ZkMpcCoreApplication {

	public static void main(String[] args) {
		SpringApplication.run(ZkMpcCoreApplication.class, args);
	}

}
