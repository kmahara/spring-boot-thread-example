# Spring Boot でスレッドを利用するサンプルプログラム

## 概要

Spring Boot でスレッドを利用し、スレッドの中で Spring Boot で管理されるオブジェクトを使用したい場合、どのように実装するかを示すサンプルプログラムです。

* TestWorker1 - 間違った例。スレッドの中で redisTemplate を使用しています。
* TestWorker2 - @Async を使用した例。Spring Boot が管理するスレッドの中で redisTemplate を使用します。

## 実行手順

1. redis サーバを起動します。

```bash
docker compose up -d
```

2. ThreadtestApplication を起動します。
