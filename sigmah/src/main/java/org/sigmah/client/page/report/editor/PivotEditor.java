package org.sigmah.client.page.report.editor;

import java.util.List;

import org.sigmah.client.EventBus;
import org.sigmah.client.dispatch.Dispatcher;
import org.sigmah.client.i18n.I18N;
import org.sigmah.client.page.table.AbstractPivot;
import org.sigmah.client.util.state.StateProvider;
import org.sigmah.shared.command.GeneratePivotTable;
import org.sigmah.shared.report.content.PivotContent;
import org.sigmah.shared.report.model.Dimension;
import org.sigmah.shared.report.model.PivotTableReportElement;
import org.sigmah.shared.report.model.ReportElement;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class PivotEditor extends AbstractPivot implements AbstractEditor {

	private PivotTableReportElement table;
	
	@Inject
	public PivotEditor(EventBus eventBus, Dispatcher service, StateProvider stateMgr) {
		super(eventBus, service, stateMgr);
	}
	
	public void bindReportElement(final PivotTableReportElement table) {

		this.table = table;
		
		treePanel.getSelectionModel().deselectAll();
		
		List<ModelData> dimStore = getDimensionStore().getModels();		
		List<Dimension> colDims = table.getColumnDimensions();
		List<Dimension> rowDims = table.getRowDimensions();
		
		for(ModelData dim : dimStore){
			
			if(rowDims.contains(dim)){
				treePanel.setChecked(dim, true);
			}
			
			if(colDims.contains(dim)){
				treePanel.setChecked(dim, true);
			}

				
		}
		
		getColStore().removeAll();
		getRowStore().removeAll();
		
		for(ModelData dim : dimStore){
			
			if(rowDims.contains(dim)){
				treePanel.setChecked(dim, true);
				getRowStore().add((Dimension) dim);
			}
			
			if(colDims.contains(dim)){
				treePanel.setChecked(dim, true);
				getColStore().add((Dimension) dim);
			}
				
		}
		
		treePanel.expandAll();

		FilterPanelHandler filtersUpdater = new FilterPanelHandler(table);
		filtersUpdater.addAdminPanelListener(adminPanel);
		filtersUpdater.addIndicatorPanelListener(indicatorPanel);
		filtersUpdater.addIndicatorTreeChangeListener(indicatorPanel, eventBus);
		filtersUpdater.addPartnerPanelListener(partnerPanel);
		filtersUpdater.updateDate(datePanel);
		
//		onUIAction(UIActions.REFRESH);
		
		service.execute(new GeneratePivotTable(table), getMonitor(),
				new AsyncCallback<PivotContent>() {
					public void onFailure(Throwable throwable) {
						MessageBox.alert(I18N.CONSTANTS.error(),
								I18N.CONSTANTS.errorOnServer(), null);
					}

					public void onSuccess(PivotContent content) {
						table.setContent((PivotContent) content);
						setContent(table);
					}
				});

	}

	@Override
	public Object getWidget() {
		return this;
	}

	@Override
	public void bindReportElement(ReportElement element) {
		bindReportElement((PivotTableReportElement) element);
	}

	@Override
	public ReportElement getReportElement() {
		return createElement();
	}

}