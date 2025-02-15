# 开始

## 创建SimpleScript文件
创建一个后缀为`.ss`的文件

## main方法
### 方法
```
方法声明
方法主体
```
#### 方法声明
他长这样: `#方法名 参数#`
### main方法
方法名: `main`

参数类型: `strings`

例子: `#main strings args#`

例子:
```
#main strings args#
print("Hello World!!!")
```
## 运行

命令:
```
java -jar SimpleScript.jar <文件名> [参数]
```
例子:
```shell
java -jar SimpleScript.jar main.ss
```

## 语句
他长这样:
```
运行(参数1,参数2...);
```
例子:
```
print("Hello World!!!");
```