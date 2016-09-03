package it.miriade.flume.source.stackexchange;

import com.google.code.stackexchange.schema.SchemaEntity;

public interface Listener {

	void send(SchemaEntity e);

}