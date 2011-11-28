package org.sigmah.client.page.common;

import java.util.List;

import org.sigmah.client.i18n.I18N;
import org.sigmah.client.page.common.widget.MappingComboBox;
import org.sigmah.shared.report.model.ReportFrequency;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.ListField;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.extjs.gxt.ui.client.widget.form.TextField;

public class SubscribeForm extends FormPanel {

	private ListStore<EmailModelData> store;
	private final static String EMAIL_VALIDATION_REGEX = "[a-z0-9!#$%&'*+/ =?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?";

	private TextField<String> title;
	private Radio weekly;
	private Radio monthly;
	private RadioGroup emailFrequency;
	private MappingComboBox dayOfWeek;
	private MappingComboBox dayOfMonth;
	private ListField<EmailModelData> subscribers;
	private TextField<String> newEmail;
	private Button addEmail;
	private Button removeEmail;

	public SubscribeForm() {

		createLayout();

	}

	public void createLayout() {

		setLabelWidth(110);

		title = new TextField<String>();
		title.setWidth("300px");
		title.setFieldLabel(I18N.CONSTANTS.title());
		title.setAllowBlank(false);
		title.setName("title");
		add(title);

		weekly = new Radio();
		weekly.setBoxLabel(I18N.CONSTANTS.weekly());
		weekly.setValue(true);
		monthly = new Radio();
		monthly.setBoxLabel(I18N.CONSTANTS.monthly());

		emailFrequency = new RadioGroup();
		emailFrequency.setFieldLabel(I18N.CONSTANTS.emailFrequency());
		emailFrequency.setOrientation(Orientation.VERTICAL);
		emailFrequency.add(weekly);
		emailFrequency.add(monthly);

		add(emailFrequency);

		dayOfWeek = new MappingComboBox();
		dayOfWeek.setAllowBlank(false);
		dayOfWeek.setFieldLabel(I18N.CONSTANTS.dayOfWeek());
		dayOfWeek.add(2, I18N.CONSTANTS.monday());
		dayOfWeek.add(3, I18N.CONSTANTS.tuesday());
		dayOfWeek.add(4, I18N.CONSTANTS.wednesday());
		dayOfWeek.add(5, I18N.CONSTANTS.thursday());
		dayOfWeek.add(6, I18N.CONSTANTS.friday());
		dayOfWeek.add(7, I18N.CONSTANTS.saturday());
		dayOfWeek.add(1, I18N.CONSTANTS.sunday());
		add(dayOfWeek);

		dayOfMonth = new MappingComboBox();
		dayOfMonth.hide();
		dayOfMonth.setFieldLabel(I18N.CONSTANTS.dayOfMonth());
		for (int i = 1; i <= 31; i++) {
			dayOfMonth.add(i, String.valueOf(i));
		}
		add(dayOfMonth);

		emailFrequency.addListener(Events.Change, new Listener<BaseEvent>() {
			@Override
			public void handleEvent(BaseEvent be) {
				if (weekly.getValue()) {
					dayOfMonth.hide();
					dayOfMonth.setAllowBlank(true);
					dayOfWeek.setAllowBlank(false);
					dayOfWeek.show();

				} else if (monthly.getValue()) {
					dayOfWeek.hide();
					dayOfWeek.setAllowBlank(true);
					dayOfMonth.setAllowBlank(false);
					dayOfMonth.show();
				}
			}
		});

		store = new ListStore<EmailModelData>();

		subscribers = new ListField<EmailModelData>();
		subscribers.setFieldLabel(I18N.CONSTANTS.subscribers());
		subscribers.setDisplayField("email");
		subscribers.setStore(store);
		subscribers
				.addSelectionChangedListener(new SelectionChangedListener<EmailModelData>() {

					@Override
					public void selectionChanged(
							SelectionChangedEvent<EmailModelData> se) {
						removeEmail.setEnabled(true);
					}
				});

		add(subscribers);

		newEmailPanel();

	}

	public void newEmailPanel() {

		HorizontalPanel hp = new HorizontalPanel();

		HorizontalPanel pf = new HorizontalPanel();
		pf.setWidth(115);
		hp.add(pf);

		newEmail = new TextField<String>();
		newEmail.setValue(I18N.CONSTANTS.enterNewEmail());
		hp.add(newEmail);

		addEmail = new Button();
		addEmail.setText(I18N.CONSTANTS.add());
		addEmail.setEnabled(false);
		hp.add(addEmail);

		addEmail.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				boolean valid = newEmail.getValue().matches(
						EMAIL_VALIDATION_REGEX);
				if (valid) {
					store.add(new EmailModelData(newEmail.getValue()));
					newEmail.setValue(I18N.CONSTANTS.enterNewEmail());
					addEmail.setEnabled(false);
				}
			}
		});
		newEmail.addListener(Events.Focus, new Listener<BaseEvent>() {
			@Override
			public void handleEvent(BaseEvent be) {
				newEmail.setValue("");
				addEmail.setEnabled(true);
			}
		});

		HorizontalPanel pm = new HorizontalPanel();
		pm.setWidth(50);
		hp.add(pm);

		removeEmail = new Button();
		removeEmail.setText(I18N.CONSTANTS.remove());
		removeEmail.setEnabled(false);
		hp.add(removeEmail);

		removeEmail.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				store.remove(subscribers.getSelection().get(0));
				removeEmail.setEnabled(false);
			}
		});

		add(hp);
	}

	@Override
	public String getTitle() {
		return title.getValue();
	}

	public ReportFrequency getReportFrequency() {
		if (monthly.getValue()) {
			return ReportFrequency.Monthly;
		} else {
			return ReportFrequency.Weekly;
		}
	}

	public int getDay() {
		if (monthly.getValue()) {
			return (Integer) ((ModelData) dayOfMonth.getSelection().get(0)).get("value");
		} else {
			return (Integer) ((ModelData) dayOfWeek.getSelection().get(0)).get("value");
		}
	}

	public String getEmailList() {

		List<EmailModelData> emailsList = store.getModels();
		int listSize = store.getModels().size();
		StringBuilder sb = new StringBuilder();
		for (int i = 1; i <= listSize; i++) {
			sb.append(emailsList.get(i - 1).getEmail());
			if (i != listSize) {
				sb.append(",");
			}
		}
		return sb.toString();

	}

	public boolean validListField() {
		if (store.getModels().size() == 0) {
			return false;
		} else {
			return true;
		}
	}

	class EmailModelData extends BaseModelData {

		public EmailModelData(String email) {
			setEmail(email);
		}

		public void setEmail(String email) {
			set("email", email);
		}

		public String getEmail() {
			return (String) get("email");
		}
	}

}