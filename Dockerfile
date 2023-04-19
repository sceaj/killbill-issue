FROM docker.artifactory.squaretrade.com/killbill/killbill:0.24.1

COPY killbill-test-plugin-0.24.1.jar /var/lib/killbill/bundles/plugins/java/killbill-test-plugin/0.24.1/
