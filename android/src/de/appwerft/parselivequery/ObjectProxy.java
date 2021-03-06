package de.appwerft.parselivequery;

import java.util.List;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.KrollModule;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.Log;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseLiveQueryClient;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SubscriptionHandling;

import de.appwerft.parselivequery.utils.GenericClass;

// This proxy can be created by calling Parselivequery.createExample({message: "hello world"})
@Kroll.proxy(creatableInModule = ParselivequeryModule.class)
public class ObjectProxy extends KrollProxy {
	// Standard Debugging variables
	private static final String LCAT = "PLQ";

	static final int MAX_CHAT_MESSAGES_TO_SHOW = 50;
	private String CLASSNAME;

	// empty Constructor
	public ObjectProxy() {
		super();
	}

	// constructor parameter import:
	@Override
	public void handleCreationArgs(KrollModule createdInModule, Object[] args) {
		if (args.length == 1 && args[0] instanceof String) {
			CLASSNAME = (String) args[0];
			Log.d(LCAT, "class " + (String) args[0] + " created");
		}
	}

	// save to parse >>>>>>>>>>
	@Kroll.method
	public void save(KrollDict opts) {
		KrollCallbacks kcb = new KrollCallbacks(opts);
		KrollDict data = opts.getKrollDict("data");
		ParseObject object = ParseObject.create("CLASSNAME");
		object.put("userId", ParseUser.getCurrentUser().getObjectId());
		object.put("body", opts);
		object.saveInBackground(new SaveCallback() {
			@Override
			public void done(ParseException e) {
				if (e == null) {
					kcb.onSuccess.call(getKrollObject(), new KrollDict());
				} else {
					kcb.onError.call(getKrollObject(), new KrollDict());
				}
			}
		});
	}

	// querying of parse:
	@Kroll.method
	public void find(KrollDict opts) {
		ParseQuery<ParseObject> query;
		// importing of callbacks:
		KrollCallbacks kcb = new KrollCallbacks(opts);
		// importingof query proxy
		if (opts.containsKeyAndNotNull(ParselivequeryModule.QUERY)) {
			Object o = opts.get(ParselivequeryModule.QUERY);
			if (o instanceof QueryProxy) {
				query = ((QueryProxy) o).query;
				findHandler(query, kcb);
			}
		}
	}

	@Kroll.method
	public void register(KrollDict opts) {
		ParseQuery<ParseObject> query;
		// importing of callbacks:
		KrollCallbacks kcb = new KrollCallbacks(opts);
		// importingof query proxy
		if (opts.containsKeyAndNotNull(ParselivequeryModule.QUERY)) {
			Object o = opts.get(ParselivequeryModule.QUERY);
			if (o instanceof QueryProxy) {
				query = ((QueryProxy) o).query;
				registerHandler(query, kcb);
			}
		}
	}

	@Kroll.method
	public void unregister(KrollDict opts) {
		// importing of callbacks:
		KrollCallbacks kcb = new KrollCallbacks(opts);

	}

	private void findHandler(ParseQuery<ParseObject> query, KrollCallbacks kcb) {
		query.findInBackground(new FindCallback<ParseObject>() {
			public void done(List<ParseObject> messages, ParseException e) {
				if (e == null) {
					KrollDict res = new KrollDict();
					res.put("messages", messages.toArray());
					if (kcb.onSuccess != null)
						kcb.onSuccess.call(getKrollObject(), res);
				} else {

					Log.e("message", "Error Loading Messages" + e);
				}
			}
		});
	}

	private void registerHandler(ParseQuery<ParseObject> query,
			KrollCallbacks kcb) {
		ParseLiveQueryClient parseLiveQueryClient = ParseLiveQueryClient.Factory
				.getClient();
		SubscriptionHandling<ParseObject> subscriptionHandling = parseLiveQueryClient
				.subscribe(query);
		subscriptionHandling
				.handleEvents(new SubscriptionHandling.HandleEventsCallback<ParseObject>() {
					@Override
					public void onEvents(ParseQuery<ParseObject> query,
							SubscriptionHandling.Event event, ParseObject object) {
						// HANDLING all events
					}
				});
	}

}