fun ktTest() {
    Test.<caret>foo("SomeTest")
}

//INFO: <div class='definition'><pre><i><span style="color:#808000;">@</span><a href="psi_element://org.jetbrains.annotations.Contract"><code><span style="color:#808000;">Contract</span></code></a><span style="">(</span><span style="">value</span><span style=""> = </span><span style="color:#008000;font-weight:bold;">"_&#32;-&gt;&#32;new"</span><span style="">,&nbsp;</span><span style="">pure</span><span style=""> = </span><span style="color:#000080;font-weight:bold;">true</span><span style="">)</span></i><a href="https://www.jetbrains.com/help/idea/annotating-source-code.html#bundled-annotations"><icon src="AllIcons.Ide.External_link_arrow"></a>&nbsp;
//INFO: <i><span style="color:#808000;">@</span><a href="psi_element://org.jetbrains.annotations.NotNull"><code><span style="color:#808000;">NotNull</span></code></a></i><a href="https://www.jetbrains.com/help/idea/annotating-source-code.html#bundled-annotations"><icon src="AllIcons.Ide.External_link_arrow"></a>&nbsp;
//INFO: <span style="color:#000080;font-weight:bold;">public static</span>&nbsp;<a href="psi_element://java.lang.Object"><code><span style="color:#000000;">Object</span></code></a><span style="">[]</span>&nbsp;<span style="color:#000000;">foo</span><span style="">(</span><br>    <a href="psi_element://java.lang.String"><code><span style="color:#000000;">String</span></code></a>&nbsp;<span style="">param</span><br><span style="">)</span></pre></div><div class='content'>
//INFO:   Java Method
//INFO:      </div><table class='sections'></table><div class="bottom"><icon src="AllIcons.Nodes.Class">&nbsp;<a href="psi_element://Test"><code><span style="color:#000000;">Test</span></code></a></div>
