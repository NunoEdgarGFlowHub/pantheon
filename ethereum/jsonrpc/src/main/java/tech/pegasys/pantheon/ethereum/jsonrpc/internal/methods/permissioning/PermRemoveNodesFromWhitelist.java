/*
 * Copyright 2018 ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package tech.pegasys.pantheon.ethereum.jsonrpc.internal.methods.permissioning;

import tech.pegasys.pantheon.ethereum.jsonrpc.internal.JsonRpcRequest;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.methods.JsonRpcMethod;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.parameters.JsonRpcParameter;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.parameters.StringListParameter;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcError;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcErrorResponse;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcResponse;
import tech.pegasys.pantheon.ethereum.jsonrpc.internal.response.JsonRpcSuccessResponse;
import tech.pegasys.pantheon.ethereum.p2p.P2pDisabledException;
import tech.pegasys.pantheon.ethereum.p2p.api.P2PNetwork;
import tech.pegasys.pantheon.ethereum.p2p.peers.DefaultPeer;
import tech.pegasys.pantheon.ethereum.p2p.permissioning.NodeWhitelistController;

import java.util.List;
import java.util.stream.Collectors;

public class PermRemoveNodesFromWhitelist implements JsonRpcMethod {

  private final P2PNetwork p2pNetwork;
  private final JsonRpcParameter parameters;

  public PermRemoveNodesFromWhitelist(
      final P2PNetwork p2pNetwork, final JsonRpcParameter parameters) {
    this.p2pNetwork = p2pNetwork;
    this.parameters = parameters;
  }

  @Override
  public String getName() {
    return "perm_removeNodesFromWhitelist";
  }

  @Override
  public JsonRpcResponse response(final JsonRpcRequest req) {
    final StringListParameter enodeListParam =
        parameters.required(req.getParams(), 0, StringListParameter.class);

    try {
      List<DefaultPeer> peers =
          enodeListParam
              .getStringList()
              .parallelStream()
              .map(this::parsePeer)
              .collect(Collectors.toList());

      NodeWhitelistController.NodesWhitelistResult nodesWhitelistResult =
          p2pNetwork.getNodeWhitelistController().removeNodes(peers);

      switch (nodesWhitelistResult.result()) {
        case SUCCESS:
          return new JsonRpcSuccessResponse(req.getId(), true);
        case REMOVE_ERROR_ABSENT_ENTRY:
          return new JsonRpcErrorResponse(req.getId(), JsonRpcError.NODE_WHITELIST_MISSING_ENTRY);
        default:
          throw new Exception();
      }
    } catch (IllegalArgumentException e) {
      return new JsonRpcErrorResponse(req.getId(), JsonRpcError.NODE_WHITELIST_INVALID_ENTRY);
    } catch (P2pDisabledException e) {
      return new JsonRpcErrorResponse(req.getId(), JsonRpcError.P2P_DISABLED);
    } catch (Exception e) {
      return new JsonRpcErrorResponse(req.getId(), JsonRpcError.INTERNAL_ERROR);
    }
  }

  private DefaultPeer parsePeer(final String enodeURI) {
    return DefaultPeer.fromURI(enodeURI);
  }
}
