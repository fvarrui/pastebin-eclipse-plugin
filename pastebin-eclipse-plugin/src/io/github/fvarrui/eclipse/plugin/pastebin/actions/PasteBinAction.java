package io.github.fvarrui.eclipse.plugin.pastebin.actions;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionDelegate;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.eclipse.ui.texteditor.ITextEditor;

import io.github.fvarrui.eclipse.plugin.pastebin.PluginActivator;
import io.github.fvarrui.eclipse.plugin.pastebin.preferences.PreferenceConstants;
import io.github.fvarrui.eclipse.plugin.pastebin.utils.HttpUtils;

public class PasteBinAction extends ActionDelegate implements IEditorActionDelegate {
	
	private ILog log = Platform.getLog(getClass());
	private String selectionText = null;
	private Shell shell;

	@Override
	public void run(IAction action) {
		
		log.info("running action: " + action.getId());

		IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();			
		
		String filename = editor.getTitle();			
		String type = filename.substring(filename.lastIndexOf('.') + 1);
		pastebin(this.selectionText, type);

		log.info("action finished!");
		
	}

	private void pastebin(String text, String type) {
		
		log.info("pastebin text: " + text);
		log.info("pastebin type: " + type);

		IPreferenceStore prefs = new ScopedPreferenceStore(InstanceScope.INSTANCE, PluginActivator.PLUGIN_ID);
		String apiKey = prefs.getString(PreferenceConstants.API_DEV_KEY);
		
		if (apiKey == null || apiKey.trim().isEmpty()) {
			MessageDialog.openError(
					shell, 
					PluginActivator.PLUGIN_NAME, 
					"You must set an API KEY in PasteBin Plugin preferences page."
				);
			return;
		}
		
        Map<String, Object> data = new HashMap<>();
        data.put("api_dev_key", apiKey);
        data.put("api_option", "paste");
        data.put("api_paste_code", text);
        if (type != null && !type.isEmpty()) {
            data.put("api_paste_format", type);        	
        }

	    HttpRequest request = HttpRequest.newBuilder()
	    		.uri(URI.create("https://pastebin.com/api/api_post.php"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .version(Version.HTTP_2)
	    		.POST(HttpUtils.ofFormData(data))
	    		.timeout(Duration.ofSeconds(5))
	    		.build();

		log.info("send request: " + request);
	    
		try {
			
			HttpResponse<String> response = HttpClient.newHttpClient().send(request, BodyHandlers.ofString(StandardCharsets.UTF_8));
			
			if (response.statusCode() == 200) {
				
				String url = response.body();
				log.info("copying url to clipboard: " + url);
				copyToClipboard(url);					
				log.info("showing info message");
				MessageDialog.openInformation(
						shell, 
						PluginActivator.PLUGIN_NAME, 
						"Your selection has been copied to PasteBin." + "\n" +
						"The URL " + url + " is on your clipboard."
				);

			} else {
				
				String error = response.body();
				MessageDialog.openError(
					shell, 
					PluginActivator.PLUGIN_NAME, 
					"Error creating paste:\n" + error
				);
				
				log.error("error creating paste: " + error);				
				
			}
			
		} catch (IOException | InterruptedException e) {
			
			MessageDialog.openError(
				shell, 
				PluginActivator.PLUGIN_NAME, 
				"Error creating paste:\n" + e.getMessage()
			);

			log.error("error creating paste: " + e.getMessage());				
			
		}
		
	}

	private void copyToClipboard(String content) {
		Clipboard cb = new Clipboard(this.shell.getDisplay());
		cb.setContents(new Object[] { content }, new Transfer[] { TextTransfer.getInstance() });
		cb.dispose();
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		if (ITextSelection.class.isAssignableFrom(selection.getClass())) {
			ITextSelection txtSelection = (ITextSelection) selection;
			if (txtSelection == null || txtSelection.isEmpty() || txtSelection.getText().trim().equals("")) {
				IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
			    IEditorInput input = (IEditorInput) editor.getEditorInput();
				IDocument document = ((ITextEditor)editor).getDocumentProvider().getDocument(input);
				this.selectionText = document.get();				
			} else {
				this.selectionText = txtSelection.getText();
			}
		}
	}

	@Override
	public void setActiveEditor(IAction action, IEditorPart editorPart) {
		shell = editorPart.getSite().getShell();
	}

}
