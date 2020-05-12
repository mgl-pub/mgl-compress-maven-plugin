# mgl-compress-maven-plugin
压缩spring boot static目录下的静态文件的 maven插件，只测试spring boot目录下static下的文件压缩

1. maven plugins 模块加入如下即可使用

```maven
                <plugin>
                    <groupId>tech.mgl</groupId>
                    <artifactId>mgl-compress-maven-plugin</artifactId>
                    <version>1.0-RELEASE</version>
                    <configuration>
                        <overWrite>true</overWrite>
                    </configuration>
                    <executions>
                        <execution>
                            <id>i</id>
                            <goals>
                                <goal>compress</goal>
                            </goals>
                            <phase>install</phase>
                        </execution>
                        <execution>
                            <id>d</id>
                            <goals>
                                <goal>compress</goal>
                            </goals>
                            <phase>deploy</phase>
                        </execution>
                        <execution>
                            <id>p</id>
                            <goals>
                                <goal>compress</goal>
                            </goals>
                            <phase>package</phase>
                        </execution>
                        <execution>
                            <id>pr</id>
                            <goals>
                                <goal>compress</goal>
                            </goals>
                            <phase>process-resources</phase>
                        </execution>
                    </executions>
                </plugin>
```

2. 参数说明

   overWrite 是否用压缩的内容覆盖原文件，true覆盖,  否则文件所在目录下生成新文件名， 例如 demo.js 会生成demo.min.js 