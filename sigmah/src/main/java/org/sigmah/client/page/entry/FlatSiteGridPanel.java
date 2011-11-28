/*
 * All Sigmah code is released under the GNU General Public License v3
 * See COPYRIGHT.txt and LICENSE.txt.
 */

package org.sigmah.client.page.entry;

import org.sigmah.client.dispatch.AsyncMonitor;
import org.sigmah.client.dispatch.Dispatcher;
import org.sigmah.client.dispatch.monitor.MaskingAsyncMonitor;
import org.sigmah.client.i18n.I18N;
import org.sigmah.shared.command.Filter;
import org.sigmah.shared.command.GetSites;
import org.sigmah.shared.command.result.SiteResult;
import org.sigmah.shared.dto.SiteDTO;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.LoadEvent;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.PagingLoader;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.LoadListener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.EditorGrid;
import com.extjs.gxt.ui.client.widget.grid.EditorGrid.ClicksToEdit;
import com.extjs.gxt.ui.client.widget.grid.GridSelectionModel;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.tips.QuickTip;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

/** 
 * Displays of sites in a "flat" projection with a paging toolbar. 
 * Note: do not use this component directly. Use the SiteGridPanel component. 
 *
 */
final class FlatSiteGridPanel extends ContentPanel implements SiteGridPanelView {
    private final Dispatcher dispatcher;
	
	private EditorGrid<SiteDTO> editorGrid;
    private ListStore<SiteDTO> listStore;
    private PagingToolBar pagingToolBar;

    private Filter currentFilter = new Filter();
    
    private final AsyncMonitor loadingMonitor = new MaskingAsyncMonitor(this, I18N.CONSTANTS.loading());
    
    	
    @Inject
    public FlatSiteGridPanel(Dispatcher dispatcher) {
    	this.dispatcher = dispatcher;
    	
    	setHeaderVisible(false);
    	setLayout(new FitLayout());
    	
    	pagingToolBar = new PagingToolBar(50);
    	setBottomComponent(pagingToolBar);
    }
	    
	public void initGrid(Filter filter, ColumnModel columnModel) {
		
		PagingLoader<PagingLoadResult<SiteDTO>> loader = new BasePagingLoader<PagingLoadResult<SiteDTO>>(new SiteProxy());
		loader.addLoadListener(new LoadListener() {

			@Override
			public void loaderLoadException(LoadEvent le) {
				Log.debug("Exception thrown during load of FlatSiteGrid: ", le.exception);
			}
			
		});
		pagingToolBar.bind(loader);
		listStore = new ListStore<SiteDTO>(loader);
    	
    	if(editorGrid == null) {
    		editorGrid = new EditorGrid<SiteDTO>(listStore, columnModel);
	    	editorGrid.setLoadMask(true);
	    	editorGrid.setStateful(true);
			editorGrid.setClicksToEdit(ClicksToEdit.TWO);
			editorGrid.setStripeRows(true);

	    	GridSelectionModel<SiteDTO> sm = new GridSelectionModel<SiteDTO>();
	        sm.setSelectionMode(SelectionMode.SINGLE);
	        sm.addSelectionChangedListener(new SelectionChangedListener<SiteDTO>() {
				
				@Override
				public void selectionChanged(SelectionChangedEvent<SiteDTO> se) {
					fireEvent(Events.SelectionChange, se);
				}
			});
	        editorGrid.setSelectionModel(sm);
	        
	        
	        QuickTip quickTip = new QuickTip(editorGrid);
	        
	        add(editorGrid, new BorderLayoutData(Style.LayoutRegion.CENTER));
	        layout();

    	} else {
    		editorGrid.reconfigure(listStore, columnModel);
    	}
    	
    	this.currentFilter = filter;
    	
    	loader.load();
	}
	
	@Override
	public void addSelectionChangeListener(SelectionChangedListener<SiteDTO> listener) {
		addListener(Events.SelectionChange, listener);
	}
	
	private class SiteProxy extends RpcProxy<PagingLoadResult<SiteDTO>> {

		@Override
		protected void load(Object loadConfig,
				final AsyncCallback<PagingLoadResult<SiteDTO>> callback) {

			PagingLoadConfig config = (PagingLoadConfig)loadConfig;
			GetSites command = new GetSites();
			command.setOffset(config.getOffset());
			command.setLimit(config.getLimit());
			command.setFilter(currentFilter);
			command.setSortInfo(config.getSortInfo());
			dispatcher.execute(command, null, new AsyncCallback<SiteResult>() {

				@Override
				public void onFailure(Throwable caught) {
					callback.onFailure(caught);
				}

				@Override
				public void onSuccess(SiteResult result) {
					callback.onSuccess(result);
				}
			});
		}
	}

	@Override
	public void refresh() {
		listStore.getLoader().load();
	}

	@Override
	public Component asComponent() {
		return this;
	}

	@Override
	public SiteDTO getSelection() {
		return editorGrid.getSelectionModel().getSelectedItem();
	}
}