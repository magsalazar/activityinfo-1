package org.sigmah.client.page.entry;

import org.sigmah.client.dispatch.Dispatcher;
import org.sigmah.client.i18n.I18N;
import org.sigmah.client.page.common.dialog.FormDialogCallback;
import org.sigmah.client.page.common.dialog.FormDialogImpl;
import org.sigmah.client.page.common.toolbar.ActionListener;
import org.sigmah.client.page.common.toolbar.UIActions;
import org.sigmah.client.util.UUID;
import org.sigmah.shared.command.DeleteSiteAttachment;
import org.sigmah.shared.command.result.VoidResult;
import org.sigmah.shared.dto.SiteDTO;

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FormEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.form.FormPanel.Encoding;
import com.extjs.gxt.ui.client.widget.form.FormPanel.Method;
import com.extjs.gxt.ui.client.widget.form.HiddenField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class AttachmentsPresenter implements ActionListener {

	public interface View {
		void setSelectionTitle(String title);

		void setActionEnabled(String id, boolean enabled);

		void setAttachmentStore(int siteId);

		String getSelectedItem();

		void refreshList();
	}

	private final View view;
	private final Dispatcher dispatcher;
	private AttachmentForm form;
	private SiteDTO currentSite;
	private String blobid;

	@Inject
	public AttachmentsPresenter(Dispatcher service, View view) {
		this.dispatcher = service;
		this.view = view;
	}

	public void showSite(SiteDTO site) {
		currentSite = site;
		view.setSelectionTitle(currentSite.getLocationName());
		view.setActionEnabled(UIActions.upload, true);
		view.setActionEnabled(UIActions.delete, false);
		view.setAttachmentStore(currentSite.getId());
	}

	@Override
	public void onUIAction(String actionId) {
		if (UIActions.delete.equals(actionId)) {
			onDelete();
		} else if (UIActions.upload.equals(actionId)) {
			onUpload();
		}

	}

	public void onUpload() {

		form = new AttachmentForm();
		form.setEncoding(Encoding.MULTIPART);
		form.setMethod(Method.POST);

		HiddenField<String> blobField = new HiddenField<String>();
		blobField.setName("blobId");
		blobid = UUID.uuid();
		blobField.setValue(blobid);
		form.add(blobField);

		final FormDialogImpl dialog = new FormDialogImpl(form);
		dialog.setWidth(400);
		dialog.setHeight(200);
		dialog.setHeading(I18N.CONSTANTS.newAttachment());

		dialog.show(new FormDialogCallback() {
			@Override
			public void onValidated() {
				form.setAction("/ActivityInfo/attachment?blobId=" + blobid
						+ "&siteId=" + currentSite.getId());
				form.submit();
				dialog.getSaveButton().setEnabled(false);
			}
		});

		form.addListener(Events.Submit, new Listener<FormEvent>() {

			@Override
			public void handleEvent(FormEvent event) {
				dialog.hide();
				view.setAttachmentStore(currentSite.getId());
			}
		});

	}

	public void onDelete() {

		DeleteSiteAttachment attachment = new DeleteSiteAttachment();
		attachment.setBlobId(view.getSelectedItem());

		dispatcher.execute(attachment, null, new AsyncCallback<VoidResult>() {
			@Override
			public void onFailure(Throwable caught) {
				// callback.onFailure(caught);
			}

			@Override
			public void onSuccess(VoidResult result) {
				view.setActionEnabled(UIActions.delete, false);
				view.setAttachmentStore(currentSite.getId());
			}
		});
	}

}