load("@rules_jvm_external//:defs.bzl", "artifact")

exports_files([
    "LICENSE",
    ".blazeproject",
    "WORKSPACE",
])

MANIFEST = "src/main/AndroidManifest.xml"

MANIFEST_DEBUG = "src/main/AndroidManifestDebug.xml"

PACKAGE = "com.afwsamples.testdpc"

java_import(
    name = "knoxsdk_lib",
    jars = ["libs/knoxsdk.jar"],
    visibility = ["//visibility:public"],
)

genrule(
    name = "generate_build_config",
    outs = ["BuildConfig.java"],
    cmd = """
cat > $@ <<EOF
package com.afwsamples.testdpc;

public final class BuildConfig {
    public static final boolean DEBUG = false;
    public static final String APPLICATION_ID = "com.afwsamples.testdpc";
    public static final String BUILD_TYPE = "release";
    public static final String VERSION_NAME = "5.9.93";
    public static final int VERSION_CODE = 1;
}
EOF
    """,
)

android_library(
    name = "buildconfig",
    srcs = [":generate_build_config"],
    custom_package = "com.example.testdpc",
)

android_library(
    name = "androidx_deps",
    exports = [
        "@maven//:androidx_annotation_annotation",
        "@maven//:androidx_appcompat_appcompat",
        "@maven//:androidx_collection_collection",
        "@maven//:androidx_core_core",
        "@maven//:androidx_enterprise_enterprise_feedback",
        "@maven//:androidx_legacy_legacy_support_v13",
        "@maven//:androidx_lifecycle_lifecycle_common",
        "@maven//:androidx_lifecycle_lifecycle_process",
        "@maven//:androidx_lifecycle_lifecycle_runtime",
        "@maven//:androidx_localbroadcastmanager_localbroadcastmanager",
        "@maven//:androidx_preference_preference",
        "@maven//:androidx_recyclerview_recyclerview",
    ],
)

android_library(
    name = "bouncycastle_deps",
    exports = [
        "@maven//:org_bouncycastle_bcpkix_jdk15on",
        "@maven//:org_bouncycastle_bcprov_jdk15on",
    ],
)

android_library(
    name = "guava_deps",
    exports = [
        "@maven//:com_google_guava_guava",
    ],
)

android_library(
    name = "test_deps",
    exports = [
        "@maven//:androidx_test_core",
        "@maven//:com_google_testparameterinjector_test_parameter_injector",
        "@maven//:com_google_truth_truth",
        "@maven//:org_robolectric_annotations",
        "@maven//:org_robolectric_robolectric",
        "@maven//:org_robolectric_shadows_framework",
        "@robolectric//bazel:android-all",
    ],
)

android_binary(
    name = "testdpc",
    assets = glob(["src/main/assets/**"]),
    assets_dir = "src/main/assets",
    custom_package = PACKAGE,
    dexopts = [
        "--force-jumbo",
    ],
    manifest = MANIFEST,
    multidex = "native",
    deps = [
        ":testdpc_lib",
    ],
)

android_binary(
    name = "testdpc_debug",
    assets = glob(["src/main/assets/**"]),
    assets_dir = "src/main/assets",
    custom_package = PACKAGE,
    dexopts = [
        "--force-jumbo",
    ],
    manifest = MANIFEST,
    multidex = "native",
    deps = [
        ":testdpc_lib",
        ":testdpc_lib_debug",
    ],
)

android_library(
    name = "testdpc_lib",
    srcs = glob(["src/main/java/**/*.java"]),
    custom_package = PACKAGE,
    javacopts = ["-Xep:AndroidJdkLibsChecker:OFF"],
    manifest = MANIFEST,
    resource_files = glob(["src/main/res/**"]),
    deps = [
        ":aidl",
        ":androidx_deps",
        ":bouncycastle_deps",
        ":buildconfig",
        ":guava_deps",
        ":knoxsdk_lib",
        "@setupcompat",
        "@setupdesign",
    ],
)

android_library(
    name = "testdpc_lib_debug",
    custom_package = PACKAGE,
    manifest = MANIFEST_DEBUG,
)

android_library(
    name = "aidl",
    custom_package = PACKAGE,
    idl_parcelables = [
        "src/main/aidl/android/content/res/AssetFileDescriptor.aidl",
    ],
    idl_srcs = glob(["src/main/aidl/com/afwsamples/testdpc/comp/*.aidl"]),
)

java_library(
    name = "test_utils",
    srcs = ["src/test/java/com/afwsamples/testdpc/util/flags/Utils.java"],
)

android_local_test(
    name = "PermissionsHelperTest",
    srcs = ["src/test/java/com/afwsamples/testdpc/common/PermissionsHelperTest.java"],
    custom_package = "com.afwsamples.testdpc.common",
    manifest = MANIFEST,
    deps = [
        ":test_deps",
        ":testdpc_lib",
        "@robolectric//bazel:android-all",
    ],
)

android_local_test(
    name = "AppStatesServiceTest",
    srcs = ["src/test/java/com/afwsamples/testdpc/feedback/AppStatesServiceTest.java"],
    custom_package = "com.afwsamples.testdpc.feedback",
    manifest = MANIFEST,
    deps = [
        ":androidx_deps",
        ":test_deps",
        ":testdpc_lib",
        "@robolectric//bazel:android-all",
    ],
)

android_local_test(
    name = "WifiConfigUtilTest",
    srcs = ["src/test/java/com/afwsamples/testdpc/policy/wifimanagement/WifiConfigUtilTest.java"],
    custom_package = "com.afwsamples.testdpc.policy.wifimanagement",
    manifest = MANIFEST,
    deps = [
        ":test_deps",
        ":testdpc_lib",
        "@robolectric//bazel:android-all",
    ],
)

android_local_test(
    name = "GetProvisioningModeActivityTest",
    srcs = ["src/test/java/com/afwsamples/testdpc/provision/GetProvisioningModeActivityTest.java"],
    custom_package = "com.afwsamples.testdpc.provision",
    manifest = MANIFEST,
    test_class = "com.afwsamples.testdpc.provision.GetProvisioningModeActivityTest",
    deps = [
        ":test_deps",
        ":testdpc_lib",
        "@maven//:org_robolectric_robolectric",
        "@robolectric//bazel:android-all",
    ],
)

java_test(
    name = "BooleanParserTest",
    size = "small",
    srcs = ["src/test/java/com/afwsamples/testdpc/util/flags/BooleanParserTest.java"],
    deps = [
        ":test_deps",
        ":test_utils",
        ":testdpc_lib",
    ],
)

java_test(
    name = "ByteParserTest",
    size = "small",
    srcs = ["src/test/java/com/afwsamples/testdpc/util/flags/ByteParserTest.java"],
    deps = [
        ":test_deps",
        ":test_utils",
        ":testdpc_lib",
    ],
)

java_test(
    name = "CharParserTest",
    size = "small",
    srcs = ["src/test/java/com/afwsamples/testdpc/util/flags/CharParserTest.java"],
    deps = [
        ":test_deps",
        ":test_utils",
        ":testdpc_lib",
    ],
)

java_test(
    name = "DoubleParserTest",
    size = "small",
    srcs = ["src/test/java/com/afwsamples/testdpc/util/flags/DoubleParserTest.java"],
    deps = [
        ":test_deps",
        ":test_utils",
        ":testdpc_lib",
    ],
)

java_test(
    name = "FloatParserTest",
    size = "small",
    srcs = ["src/test/java/com/afwsamples/testdpc/util/flags/FloatParserTest.java"],
    deps = [
        ":test_deps",
        ":test_utils",
        ":testdpc_lib",
    ],
)

java_test(
    name = "IntParserTest",
    size = "small",
    srcs = ["src/test/java/com/afwsamples/testdpc/util/flags/IntParserTest.java"],
    deps = [
        ":test_deps",
        ":test_utils",
        ":testdpc_lib",
    ],
)

java_test(
    name = "LongParserTest",
    size = "small",
    srcs = ["src/test/java/com/afwsamples/testdpc/util/flags/LongParserTest.java"],
    deps = [
        ":test_deps",
        ":test_utils",
        ":testdpc_lib",
    ],
)

java_test(
    name = "ShortParserTest",
    size = "small",
    srcs = ["src/test/java/com/afwsamples/testdpc/util/flags/ShortParserTest.java"],
    deps = [
        ":test_deps",
        ":test_utils",
        ":testdpc_lib",
    ],
)

java_test(
    name = "StringParserTest",
    size = "small",
    srcs = ["src/test/java/com/afwsamples/testdpc/util/flags/StringParserTest.java"],
    deps = [
        ":test_deps",
        ":test_utils",
        ":testdpc_lib",
    ],
)

java_test(
    name = "CustomParserTest",
    size = "small",
    srcs = ["src/test/java/com/afwsamples/testdpc/util/flags/CustomParserTest.java"],
    deps = [
        ":test_deps",
        ":test_utils",
        ":testdpc_lib",
    ],
)

java_test(
    name = "CallbackTest",
    size = "small",
    srcs = ["src/test/java/com/afwsamples/testdpc/util/flags/CallbackTest.java"],
    deps = [
        ":test_deps",
        ":test_utils",
        ":testdpc_lib",
    ],
)

java_test(
    name = "HelpTextGenerationTest",
    size = "small",
    srcs = ["src/test/java/com/afwsamples/testdpc/util/flags/HelpTextGenerationTest.java"],
    deps = [
        ":test_deps",
        ":test_utils",
        ":testdpc_lib",
    ],
)

java_test(
    name = "InvalidCallsTest",
    size = "small",
    srcs = ["src/test/java/com/afwsamples/testdpc/util/flags/InvalidCallsTest.java"],
    deps = [
        ":test_deps",
        ":test_utils",
        ":testdpc_lib",
    ],
)

java_test(
    name = "ParamTest",
    size = "small",
    srcs = ["src/test/java/com/afwsamples/testdpc/util/flags/ParamTest.java"],
    deps = [
        ":test_deps",
        ":test_utils",
        ":testdpc_lib",
    ],
)

java_test(
    name = "RegistrationTest",
    size = "small",
    srcs = ["src/test/java/com/afwsamples/testdpc/util/flags/RegistrationTest.java"],
    deps = [
        ":test_deps",
        ":test_utils",
        ":testdpc_lib",
    ],
)
