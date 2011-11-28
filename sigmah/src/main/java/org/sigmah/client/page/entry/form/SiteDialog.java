package org.sigmah.client.page.entry.form;

import java.util.List;

import org.sigmah.client.dispatch.Dispatcher;
import org.sigmah.client.i18n.I18N;
import org.sigmah.client.icon.IconImageBundle;
import org.sigmah.client.offline.command.handler.KeyGenerator;
import org.sigmah.client.page.entry.form.resources.SiteFormResources;
import org.sigmah.shared.command.CreateSite;
import org.sigmah.shared.command.result.CreateResult;
import org.sigmah.shared.dto.ActivityDTO;
import org.sigmah.shared.dto.LocationDTO;
import org.sigmah.shared.dto.SiteDTO;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.ListViewEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.CardLayout;
import com.google.common.collect.Lists;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class SiteDialog extends Window {
	
	public static final int HEIGHT = 450;
	public static final int WIDTH = 500;
	private FormNavigationListView navigationListView;
	private LayoutContainer sectionContainer;
	
	private List<FormSection<SiteDTO>> sections = Lists.newArrayList();
	private LocationFormSection locationForm;
	
	private final Dispatcher dispatcher;
	private final ActivityDTO activity;
	
	private SiteDialogCallback callback;
	
	private SiteDTO site = null;
	
	/**
	 * True if this is a brand new site
	 */
	private boolean newSite;
		
	public SiteDialog(Dispatcher dispatcher, ActivityDTO activity) {
		this.dispatcher = dispatcher;
		this.activity = activity;
		
		setHeading(I18N.MESSAGES.addNewSiteForActivity(activity.getName()));
		setWidth(WIDTH);
		setHeight(HEIGHT);
	
		setLayout(new BorderLayout());
		
		navigationListView = new FormNavigationListView();
		BorderLayoutData navigationLayout = new BorderLayoutData(LayoutRegion.WEST);
		navigationLayout.setSize(150);
		add(navigationListView, navigationLayout);
		
		sectionContainer = new LayoutContainer();
		final CardLayout sectionLayout = new CardLayout();
		sectionContainer.setLayout(sectionLayout);
		
		add(sectionContainer, new BorderLayoutData(LayoutRegion.CENTER));

		if(activity.getLocationType().isAdminLevel()) {
			locationForm = new BoundLocationSection(dispatcher, activity);
		} else {
			locationForm = new LocationSection(activity);
		}
		
		addSection(FormSectionModel.forComponent(new ActivitySection(activity))
				.withHeader("Site Details")
				.withDescription("Choose the project and partner implementing this activity"));
		
		addSection(FormSectionModel.forComponent(locationForm)
				.withHeader(I18N.CONSTANTS.location())
				.withDescription("Choose the location of the activity site"));

		addSection(FormSectionModel.forComponent(new AttributeSection(activity))
				.withHeader(I18N.CONSTANTS.attributes())
				.withDescription("Choose the attributes of this activity site"));
		
		addSection(FormSectionModel.forComponent(new IndicatorSection(activity))
				.withHeader(I18N.CONSTANTS.indicators())
				.withDescription("Enter indicator results for this site"));
		
		addSection(FormSectionModel.forComponent(new CommentSection())
				.withHeader(I18N.CONSTANTS.comments())
				.withDescription("Add additional comments for this activity site"));
		
			
		SiteFormResources.INSTANCE.style().ensureInjected();
		
		navigationListView.addListener(Events.Select, new Listener<ListViewEvent<FormSectionModel<SiteDTO>>>() {

			@Override
			public void handleEvent(ListViewEvent<FormSectionModel<SiteDTO>> be) {
				sectionLayout.setActiveItem(be.getModel().getComponent());
			}
		});
		
		Button finishButton = new Button(I18N.CONSTANTS.save(), IconImageBundle.ICONS.save(), new SelectionListener<ButtonEvent>() {

			@Override
			public void componentSelected(ButtonEvent ce) {
				onSaveClicked();
			}
		});

		getButtonBar().add(finishButton);
	}
	
	public void showNew(SiteDTO site, LocationDTO location, boolean locationIsNew, SiteDialogCallback callback) {
		this.newSite = true;
		this.callback = callback;
		locationForm.updateForm(location, locationIsNew);
		updateForms(site);
		show();
	}

	private void updateForms(SiteDTO site) {
		for(FormSectionModel<SiteDTO> section : navigationListView.getStore().getModels()) {
			section.getSection().updateForm(site);
		}
	}
	
	public void showExisting(SiteDTO site, SiteDialogCallback callback) {
		this.newSite = false;
		this.site = site;
		this.callback = callback;
		updateForms(site);
		show();
	}
	
	
	private void addSection(FormSectionModel<SiteDTO> model) {
		navigationListView.addSection(model);
		sectionContainer.add(model.getComponent());
		sections.add(model.getSection());
	}
	
	private void onSaveClicked() {
		if(validateSections()) {
			saveLocation();
		}
	}

	private boolean validateSections() {
		for(FormSectionModel<SiteDTO> section : navigationListView.getStore().getModels()) {
			if(!section.getSection().validate()) {
				navigationListView.getSelectionModel().select(section, false);
				MessageBox.alert(getHeading(), I18N.CONSTANTS.pleaseCompleteForm(), null);
				return false;
			}
		}
		return true;
	}

	private void saveLocation() {
		// start save
		locationForm.save(new AsyncCallback<Void>() {
			
			@Override
			public void onSuccess(Void result) {
				saveSite();
			}
			
			@Override
			public void onFailure(Throwable caught) {
				MessageBox.alert(I18N.CONSTANTS.saving(), I18N.CONSTANTS.serverError(), null);
			}
		});
	}


	protected void saveSite() {
		if(newSite) {
			saveNewSite();
		} else {
			updateSite();
		}
	}

	private void saveNewSite() {
		final SiteDTO newSite = new SiteDTO();
		newSite.setId(new KeyGenerator().generateInt());
		newSite.setActivityId(activity.getId());
		
		if(activity.getReportingFrequency() == ActivityDTO.REPORT_ONCE) {
			newSite.setReportingPeriodId(new KeyGenerator().generateInt());
		}
		
		for(FormSectionModel<SiteDTO> section : navigationListView.getStore().getModels()) {
			section.getSection().updateModel(newSite);
		}
		
		dispatcher.execute(new CreateSite(newSite), null, new AsyncCallback<CreateResult>() {

			@Override
			public void onFailure(Throwable caught) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onSuccess(CreateResult result) {
				hide();
				callback.onSaved(newSite);
			}
		});
	}
	
	private void updateSite() {
		
	}

}