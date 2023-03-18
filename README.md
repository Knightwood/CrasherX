## 使用

```gradle
1. 在根build.gradle文件中添加:
 
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
  
2. 添加依赖 Tag: [![](https://jitpack.io/v/Knightwood/crasherx.svg)](https://jitpack.io/#Knightwood/crasherx)
  dependencies {
		implementation 'com.github.Knightwood:crasherx:Tag'
	}
```

### 设置异常处理程序 

创建一个 `new Crasher(context)`将自动将 Crasher 设置为当前线程中的默认异常处理程序，这需要在应用程序的每个线程中准备，或者在希望 Crasher 拦截异常的每个线程中准备。 

**注意：这样做会阻止 Play 商店的崩溃报告工作，因为 Crasher 将拦截所有异常和“此应用程序已停止工作！” 结果不会显示对话框。**  您可以使用该方法 `CrashUtils.isInstalledFromPlayStore(context)`（或者干脆 `BuildConfig.DEBUG`检查它是否是调试版本）以防止这种情况。 

建议存储一个实例 `Crasher`在您的应用程序中 [`Application`](https://developer.android.com/reference/android/app/Application.html)类，然后简单地将其传递给 `Thread.setDefaultUncaughtExceptionHandler(crasher)`在您创建的每个新线程中。  如果你不搞多线程，那么你就不用担心这个，只需要创建一个 `Crasher`，无论是在 `Application`类的 `onCreate`方法，或 打开的第一个 `Activity`的`onCreate`。 

### 选项 

#### 应用名称 

`crasherx`使用 `R.string.app_name`作为要在工具栏中显示的应用程序的名称。  它的默认值是`“CrasherX”`，但可以通过设置 strings.xml 文件中的`app_name`字符串资源进行修改。 

#### 电子邮件 

要为用户指定电子邮件以选择性地向其发送崩溃报告，只需调用 `crasher.setEmail(email)`并将所需的电子邮件作为字符串传递，或通过 `null`删除按钮（在大多数情况下这将是过时的，因为除非通过电子邮件，否则默认情况下不会显示它）。 

#### 堆栈溢出 

CrasherX 还可以选择在应用程序崩溃时在 stackoverflow 上打开异常消息的搜索结果。   这在“常规使用”期间可能会有些烦人，因为解决大多数开发人员一目了然的常见错误（例如，清单中缺少声明）需要更多时间，而这实际上只是为了展示。   您只能通过调用为应用程序的调试版本启用它 `crasher.setStackoverflowEnabled(true)`，或通过调用所有构建 `crasher.setForceStackOverflow(true)`（当我决定这两种方法的大小写时，我不确定我在想什么，但我现在懒得修复它，所以它们可能会停留一段时间）。 

#### 设置异常处理程序后启用/禁用 Crasher

调用 `crasher.setCrashActivityEnabled(isCrashThingyEnabled)`.  不言自明。 

#### 其他的东西 

您可以通过在您自己的 strings.xml 文件中指定它来覆盖应用程序使用的任何字符串资源（例如，如果您想进行自己的翻译）。  CrasherX使用的字符串如下： 

|Resource Name|Value|Used For|
|-----|-----|-----|
|app_name|Crasher|操作栏标题中的应用名称/其他随机字符串|
|title_crasher_crashed|%1$s Crashed|操作栏标题，使用 String.format|
|title_crasher_exception|%1$s in %2$s|用于电子邮件主题，例如"NullPointerException in My App"|
|msg_crasher_crashed|Unfortunately, %1$s has stopped working. You can report this as a bug using the buttons below, or you can try to fix it yourself. When reporting the bug, please include as much detail as possible to help fix the issue as quickly as possible. Please note that your device info, such as the manufacturer and model number, may be included in the bug report.|发生崩溃时向用户显示的消息|
|title_crasher_copy|Copy|将堆栈跟踪复制到剪贴板的按钮文本|
|title_crasher_share|Share|用于调出带有堆栈跟踪的共享菜单的按钮文本|
|title_crasher_email|Email|通过电子邮件共享堆栈跟踪和设备信息的按钮文本|
|title_crasher_send_email|Send Email|如果用户有多个电子邮件应用程序可供选择，则会出现共享菜单的标题|
|title_crasher_stack_trace|Stack Trace|显示/隐藏堆栈跟踪的按钮标题|
