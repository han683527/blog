# 生产 ES 镜像:基础镜像 + IK 中文分词插件
# analysis-ik 与 config-analysis-ik 是从本地正常运行的容器拷出来的
# (插件版本 7.17.28,与 ES 完全匹配),不需要联网下载插件
FROM docker.elastic.co/elasticsearch/elasticsearch:7.17.28

# 插件主程序
COPY --chown=elasticsearch:elasticsearch analysis-ik /usr/share/elasticsearch/plugins/analysis-ik
# 词典文件必须放在 config 目录,IK 才会加载
COPY --chown=elasticsearch:elasticsearch config-analysis-ik /usr/share/elasticsearch/config/analysis-ik
